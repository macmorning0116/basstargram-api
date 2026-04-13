package com.yechan.fishing.fishing_api.domain.auth.controller;

import com.yechan.fishing.fishing_api.domain.auth.cookie.RefreshTokenCookieManager;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthorizationCodeLoginRequest;
import com.yechan.fishing.fishing_api.domain.auth.dto.SocialAuthorizationUrlResponse;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthService;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthSessionResult;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  private final RefreshTokenCookieManager refreshTokenCookieManager;

  public AuthController(
      AuthService authService, RefreshTokenCookieManager refreshTokenCookieManager) {
    this.authService = authService;
    this.refreshTokenCookieManager = refreshTokenCookieManager;
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
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse) {
    AuthSessionResult result =
        authService.loginWithAuthorizationCode(
            AuthProvider.from(provider), request, httpServletRequest.getHeader("User-Agent"));
    refreshTokenCookieManager.addRefreshTokenCookie(
        httpServletResponse, result.refreshToken(), result.response().refreshTokenExpiresAt());
    return ApiResponse.success(result.response());
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthTokenResponse> refresh(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    String refreshToken = refreshTokenCookieManager.extractRefreshToken(httpServletRequest);
    AuthSessionResult result =
        authService.refresh(refreshToken, httpServletRequest.getHeader("User-Agent"));
    refreshTokenCookieManager.addRefreshTokenCookie(
        httpServletResponse, result.refreshToken(), result.response().refreshTokenExpiresAt());
    return ApiResponse.success(result.response());
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    String refreshToken = refreshTokenCookieManager.extractRefreshToken(httpServletRequest);
    authService.logout(refreshToken);
    refreshTokenCookieManager.expireRefreshTokenCookie(httpServletResponse);
    return ApiResponse.success(null);
  }
}
