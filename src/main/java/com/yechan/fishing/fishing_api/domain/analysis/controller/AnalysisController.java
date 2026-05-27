package com.yechan.fishing.fishing_api.domain.analysis.controller;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisRequest;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisUsageResponse;
import com.yechan.fishing.fishing_api.domain.analysis.service.AnalysisService;
import com.yechan.fishing.fishing_api.domain.auth.security.AuthenticatedUser;
import com.yechan.fishing.fishing_api.domain.auth.security.CurrentUser;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/analysis")
public class AnalysisController {

  private final AnalysisService analysisService;

  public AnalysisController(AnalysisService analysisService) {
    this.analysisService = analysisService;
  }

  @PostMapping("/photo")
  public ApiResponse<AnalysisResponse> analyze(
      @Valid @ModelAttribute AnalysisRequest request, @CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(
        analysisService.analyze(user.id(), request.image(), request.lat(), request.lng()));
  }

  @GetMapping("/usage")
  public ApiResponse<AnalysisUsageResponse> getUsage(@CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(analysisService.getDailyUsage(user.id()));
  }
}
