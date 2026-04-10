package com.yechan.fishing.fishing_api.domain.auth.controller;

import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthorizationCodeLoginRequest;
import com.yechan.fishing.fishing_api.domain.auth.dto.RefreshTokenRequest;
import com.yechan.fishing.fishing_api.domain.auth.dto.SocialAuthorizationUrlResponse;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @GetMapping("/{provider}/authorize-url")
  public ApiResponse<SocialAuthorizationUrlResponse> getAuthorizationUrl(
      @PathVariable String provider) {
    return ApiResponse.success(authService.getAuthorizationUrl(AuthProvider.from(provider)));
  }

  @PostMapping("/{provider}/code")
  public ApiResponse<AuthTokenResponse> loginWithAuthorizationCode(
      @PathVariable String provider,
      @Valid @RequestBody AuthorizationCodeLoginRequest request,
      HttpServletRequest httpServletRequest) {
    return ApiResponse.success(
        authService.loginWithAuthorizationCode(
            AuthProvider.from(provider), request, httpServletRequest.getHeader("User-Agent")));
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthTokenResponse> refresh(
      @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpServletRequest) {
    return ApiResponse.success(
        authService.refresh(request, httpServletRequest.getHeader("User-Agent")));
  }
}
