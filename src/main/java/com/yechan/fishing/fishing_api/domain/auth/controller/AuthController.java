package com.yechan.fishing.fishing_api.domain.auth.controller;

import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.RefreshTokenRequest;
import com.yechan.fishing.fishing_api.domain.auth.dto.SocialLoginRequest;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/social/login")
  public ApiResponse<AuthTokenResponse> socialLogin(
      @Valid @RequestBody SocialLoginRequest request, HttpServletRequest httpServletRequest) {
    return ApiResponse.success(
        authService.socialLogin(request, httpServletRequest.getHeader("User-Agent")));
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthTokenResponse> refresh(
      @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpServletRequest) {
    return ApiResponse.success(
        authService.refresh(request, httpServletRequest.getHeader("User-Agent")));
  }
}
