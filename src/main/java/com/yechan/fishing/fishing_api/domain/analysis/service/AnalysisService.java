package com.yechan.fishing.fishing_api.domain.analysis.service;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisUsageResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.external.gpt.GptClient;
import com.yechan.fishing.fishing_api.global.external.weather.WeatherClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AnalysisService {

  private final WeatherClient weatherClient;
  private final GptClient gptClient;
  private final AnalysisUsageService analysisUsageService;

  public AnalysisService(
      WeatherClient weatherClient, GptClient gptClient, AnalysisUsageService analysisUsageService) {
    this.weatherClient = weatherClient;
    this.gptClient = gptClient;
    this.analysisUsageService = analysisUsageService;
  }

  public AnalysisResponse analyze(Long userId, MultipartFile image, double lat, double lng) {
    analysisUsageService.consumeDailyUsage(userId);

    GptWeatherContext weather = weatherClient.getGptWeatherContext(lat, lng);

    AnalysisResponse response = gptClient.analyze(image, weather);

    return response;
  }

  public AnalysisUsageResponse getDailyUsage(Long userId) {
    return analysisUsageService.getDailyUsage(userId);
  }
}
