package com.yechan.fishing.fishing_api.global.external.gpt;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import com.yechan.fishing.fishing_api.global.external.gpt.dto.GptRequest;
import com.yechan.fishing.fishing_api.global.external.gpt.dto.GptResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;

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

        String base64;
        try {
            base64 = Base64.getEncoder().encodeToString(image.getBytes());
        } catch (Exception e) {
            throw new FishingException(ErrorCode.GPT_API_ERROR);
        }

        int maxRetry = 3;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            boolean isRetry = attempt > 1;

            String prompt = buildPrompt(weather, isRetry);

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

            try {
                GptResponse response = webClient.post()
                        .uri("/responses")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(GptResponse.class)
                        .block();

                String raw = response.getOutputText();
                String json = extractJson(raw);

                // JSON 구조 검증
                objectMapper.readTree(json);

                return objectMapper.readValue(json, AnalysisResponse.class);

            } catch (Exception e) {
                if (attempt == maxRetry) {
                    throw new FishingException(ErrorCode.GPT_API_ERROR);
                }
                // else: 다음 재시도
            }
        }

        // 이론상 도달 불가, 안전망
        throw new FishingException(ErrorCode.GPT_API_ERROR);
    }

    private String buildPrompt(GptWeatherContext w, boolean isRetry) {
        String base = """
        당신은 20년차 낚시 고수(포인트 분석 전문가)입니다.
        사용자가 보낸 "사진"과 아래 "환경 정보"를 종합해서, 낚시 포인트를 추천해 주세요.
        
        [말투 규칙]
        - 반드시 존댓말로만 답변해 주세요.
        - 반말/명령조/비속어는 금지합니다.
        
        [좌표 안정화 규칙]
        - x, y는 반드시 0.08 이상 0.92 이하 범위에서만 선택하세요.
        - radius는 0.05 이상 0.18 이하로 제한하세요.
        - 이미지의 가장자리(프레임, 하늘만 있는 영역, 물과 무관한 영역)는 피하세요.
        - points는 실제 낚시 접근이 가능한 위치만 선택하세요.
        
        [활성도 판단 규칙]
        - 현재 시각(timestamp)을 기준으로 아래를 고려하세요.
        - sunrise 전후 ±1시간: 활성도 높음 (아침 피딩 타임)
        - sunset 전후 ±1시간: 활성도 높음 (저녁 피딩 타임)
        - 그 외 시간대:
          - 흐림(cloudiness 높음) + 약한 바람: 중간 이상
          - 맑음 + 바람 약함: 중간
          - 바람 강함 + 수온 낮음: 낮음
        - 이 판단을 summary와 strategy에 반드시 반영하세요.
        
        [출력 규칙]
        - 반드시 아래 JSON "객체" 하나만 출력해 주세요.
        - JSON 외의 설명 문장, 마크다운, 코드블록(```), 주석은 절대 출력하지 마세요.
        - 숫자는 number로 출력하세요(따옴표로 감싸지 마세요).
        - points는 2~4개로 출력해 주세요.
        - 좌표계: 이미지 좌상단이 (0,0), 우하단이 (1,1)인 "비율 좌표"입니다.
        - radius는 0.05 ~ 0.20 범위로 출력해 주세요.
        
        [JSON 스키마]
        {
          "summary": string,
          "points": [
            { "x": number, "y": number, "radius": number, "reason": string }
          ],
          "tackle": string,
          "strategy": string
        }
        
        [작성 가이드]
        - summary: 현재 상황을 2~3문장으로 요약(날씨/바람/수온 추정/활성도 추정 포함)
        - reason: 왜 그 지점이 유리한지(그늘, 수초, 브레이크라인, 유입수, 바람 맞는 면 등)
        - tackle: 채비를 구체적으로(대상어/라인 파운드/훅/싱커/루어 종류)
        - strategy: 운용법을 구체적으로(캐스팅 각도, 수심층, 릴링 속도, 스테이/저킹, 탐색 순서)
        
        [환경 정보]
        - lat: %f
        - lng: %f
        - timestamp: %d
        - temp(°C): %.1f
        - feelsLike(°C): %.1f
        - humidity(%%): %d
        - windSpeed(m/s): %.1f
        - windDeg: %d
        - cloudiness(%%): %d
        - weatherMain: %s
        - weatherDesc: %s
        - sunrise: %d
        - sunset: %d
        """.formatted(
                w.lat(), w.lng(),
                w.timestamp(),
                w.temperature(), w.feelsLike(),
                w.humidity(),
                w.windSpeed(), w.windDeg(),
                w.cloudiness(),
                w.weatherMain(), w.weatherDesc(),
                w.sunrise(), w.sunset()
        );

        if (!isRetry) {
            return base;
        }

        // 재시도 할때 추가할 프롬프트
        return base + """
                이전 응답이 JSON 형식이 아니었습니다.
                이번에는 반드시 JSON 객체 하나만 출력하세요.
                다른 텍스트는 절대 포함하지 마세요.
                """;
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readValue(json, AnalysisResponse.class);
            return true;
        } catch (Exception e) {
            return false;
        }
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
