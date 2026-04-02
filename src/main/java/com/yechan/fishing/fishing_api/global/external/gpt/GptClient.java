package com.yechan.fishing.fishing_api.global.external.gpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import com.yechan.fishing.fishing_api.global.external.gpt.dto.GptRequest;
import com.yechan.fishing.fishing_api.global.external.gpt.dto.GptResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class GptClient {

    private final WebClient webClient;
    private final OpenAiProperties props;
    private final ObjectMapper objectMapper;

    public GptClient(
            @Qualifier("openAiWebClient") WebClient webClient,
            OpenAiProperties props,
            ObjectMapper objectMapper
    ) {
        this.webClient = webClient;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public AnalysisResponse analyze(
            MultipartFile image,
            GptWeatherContext weather
    ) {
        String mime = image.getContentType() != null
                ? image.getContentType()
                : "image/jpeg";
        String base64 = encodeBase64(image);

        int maxRetry = 3;
        boolean isDaytime = isDaytime(weather);

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            boolean isRetry = attempt > 1;
            String prompt = buildPrompt(weather, isRetry, isDaytime);

            GptRequest request = buildRequest(prompt, mime, base64);

            try {
                String raw = callGpt(request);
                String json = extractJson(raw);

                // JSON 구조 검증
                objectMapper.readTree(json);
                return objectMapper.readValue(json, AnalysisResponse.class);

            } catch (Exception e) {
                log.error("GPT attempt {} failed", attempt, e);
            }
        }
        throw new FishingException(ErrorCode.GPT_API_ERROR);
    }

    private String callGpt(GptRequest request) {
        return webClient.post()
                .uri("/responses")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GptResponse.class)
                .map(GptResponse::getOutputText)
                .switchIfEmpty(Mono.error(new FishingException(ErrorCode.GPT_API_ERROR)))
                .block();
    }

    private boolean isDaytime(GptWeatherContext weather) {
        long now = weather.timestamp();
        long sunrise = weather.sunrise();
        long sunset = weather.sunset();

        return sunrise <= now && now <= sunset;
    }

    private String encodeBase64(MultipartFile image) {
        try {
            return Base64.getEncoder().encodeToString(image.getBytes());
        } catch (Exception e) {
            throw new FishingException(ErrorCode.GPT_API_ERROR);
        }
    }


    private GptRequest buildRequest(String prompt, String mime, String base64) {
        GptRequest request = new GptRequest(
                props.getModel(),
                List.of(
                        new GptRequest.Input(
                                "user",
                                List.of(
                                        new GptRequest.TextContent("input_text", prompt),
                                        new GptRequest.ImageContent(
                                                "input_image",
                                                "data:" + mime + ";base64," + base64,
                                                "high"
                                        )
                                )
                        )
                )
        );
        return request;
    }


    private String buildPrompt(GptWeatherContext w, boolean isRetry, boolean isDaytime) {
        String timeWindowKo = isDaytime ? "주간" : "야간";

        String base = String.format(Locale.US, """
                        You are a fishing spot analyst with 20+ years of experience specialized in photo-based spot reading.
                        Use ONLY the provided photo and the environment data below to recommend fishing spots.
                        
                        [TONE + LANGUAGE]
                        - Output JSON only.
                        - JSON keys must follow the schema (English keys).
                        - ALL string values (summary, reason, tackle, strategy) MUST be Korean only (존댓말).
                        - Do NOT use any English words at all inside string values. (e.g., NIGHTTIME/DAYTIME/OK/LINE etc. 금지)
                        
                        [CRITICAL INTERPRETATION]
                        - temp is AIR temperature (대기 기온) only. It is NOT water temperature.
                        - Do NOT claim or estimate water temperature.
                        
                        [TIME WINDOW — SERVER FINAL]
                        - currentTimeWindow: %s
                        [ABSOLUTE]
                        - The time window above is FINAL. Do NOT reinterpret sunrise/sunset/timestamp.
                        - You MUST reflect this time window in summary/strategy.
                        
                        ────────────────────────────────────────
                        [OUTPUT LENGTH — MUST BE SHORT]
                        - summary: max 2 sentences, total <= 220 characters.
                        - each reason: max 2 sentences, <= 180 characters.
                        - tackle: use the REQUIRED format below, <= 380 characters.
                        - strategy: use the REQUIRED format below, max 4 lines, <= 420 characters.
                        - Do NOT add extra explanations.
                        
                        ────────────────────────────────────────
                        [POINT SELECTION — MUST NOT VIOLATE]
                        - Coordinates are normalized: top-left (0,0), bottom-right (1,1).
                        - x,y must be within [0.08, 0.92].
                        - radius must be within [0.05, 0.18].
                        - points can contain 0, 1, or 2 items.
                        - Return 0 points if fishable water is not clearly visible.
                        
                        [WATER-ONLY RULE — HIGHEST PRIORITY]
                        - Each point center (x,y) MUST be ON WATER surface.
                        - NEVER place the center on sky/land/trees/rocks/decks/roads/boats/buildings/shadows.
                        - Avoid regions above the horizon line if visible.
                        - Prefer water cues: ripples, reflections, specular highlights, continuous water texture.
                        - If water surface is too small, too dark, too blurry, blocked, or visually ambiguous, return an empty points array.
                        - It is better to return no points than to place a wrong point on land or non-water.
                        
                        [INTERNAL VALIDATION LOOP]
                        For EACH point, internally verify:
                        1) center is clearly on water (not sky/land)
                        2) nearby texture also looks like water (ripples/reflection)
                        3) not near frame/UI/text/edge
                        If any fails, move the point and recheck.
                        If all candidate points fail, return an empty points array.
                        
                        ────────────────────────────────────────
                        [TACKLE FORMAT — BEGINNER FRIENDLY]
                        Write tackle as 2~3 short lines with easy Korean labels:
                        "대상어: ..."
                        "추천 채비: ..."
                        "라인과 로드: ..."
                        
                        Rules:
                        - Worm type must be explicit (스트레이트/패들테일/크리처/호그/그럽 중 선택)
                        - Rig must be explicit (네꼬/다운샷/텍사스/지그헤드/노싱커 중 선택)
                        - Must include numeric sizes (inches, g, mm or g, lb)
                        - No vague words like "적당히", "작게", "상황 봐서", "야광미끼"
                        - Explain simply enough that a beginner can follow immediately.
                        - If points is empty, tackle MUST NOT recommend an actual lure or rig.
                        - If points is empty, tackle should be plain Korean guidance text only.
                        - If points is empty, do NOT use labels like "대상어:", "추천 채비:", "라인과 로드:".
                        - If points is empty, briefly explain that tackle recommendation is difficult from the current photo and guide the user to retake the photo with clear water surface.
                        
                        [STRATEGY FORMAT — BEGINNER FRIENDLY]
                        - Write strategy as 2~4 short lines using natural Korean labels.
                        - Preferred labels:
                          "먼저: ..."
                          "다음: ..."
                          "반응이 없으면: ..."
                          "주의할 점: ..."
                        - Avoid numbered bullets like ①②③④.
                        - Each line must be concrete and easy to follow for beginners.
                        - If points is empty, strategy MUST explain that the current photo does not show clear fishable water and briefly guide the user to retake the photo.
                        - If points is empty, strategy should be plain Korean guidance text only.
                        - If points is empty, do NOT use labels like "먼저:", "다음:", "반응이 없으면:", "주의할 점:".
                        - If points is empty, focus only on practical retake guidance such as showing water surface, shoreline boundary, and nearby structure.
                        - If points is empty, avoid overdone safety instructions unless directly visible in the photo.
                        
                        ────────────────────────────────────────
                        [JSON SCHEMA]
                        {
                          "summary": string,
                          "points": [
                            { "x": number, "y": number, "radius": number, "reason": string }
                          ],
                          "tackle": string,
                          "strategy": string
                        }
                        - points may be [] when no valid water-only point exists.
                        - If points is not empty, include at most 2 items.
                        - If points is empty, summary/tackle/strategy must not pretend that a valid spot was found.
                        
                        [ENVIRONMENT DATA]
                        - lat: %.6f
                        - lng: %.6f
                        - temp(C, air): %.1f
                        - feelsLike(C): %.1f
                        - humidity(%%): %d
                        - windSpeed(m/s): %.1f
                        - windDeg: %d
                        - cloudiness(%%): %d
                        - weatherMain: %s
                        - weatherDesc: %s
                        """,
                timeWindowKo,
                w.lat(), w.lng(),
                w.temperature(), w.feelsLike(),
                w.humidity(),
                w.windSpeed(), w.windDeg(),
                w.cloudiness(),
                w.weatherMain(), w.weatherDesc()
        );

        if (!isRetry) return base;

        return base + """
                Previous response violated rules (format/length/language/water-only).
                Return ONLY ONE JSON object matching the schema.
                Remember: string values must be Korean only and short.
                """;
    }

    private String extractJson(String s) {
        if (s == null) return "{}";
        int l = s.indexOf('{');
        int r = s.lastIndexOf('}');

        if (l != -1 && r != -1 && r > 1) {
            return s.substring(l, r + 1);
        }

        throw new FishingException(ErrorCode.GPT_RESPONSE_PARSE_ERROR);
    }

}
