package com.yechan.fishing.fishing_api.domain.auth.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieManager {

  private final AuthCookieProperties properties;

  public RefreshTokenCookieManager(AuthCookieProperties properties) {
    this.properties = properties;
  }

  public void addRefreshTokenCookie(
      HttpServletResponse response, String refreshToken, LocalDateTime expiresAt) {
    long maxAge = Math.max(0, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());

    ResponseCookie cookie =
        ResponseCookie.from(properties.getName(), refreshToken)
            .httpOnly(true)
            .secure(properties.isSecure())
            .sameSite(properties.getSameSite())
            .path(properties.getPath())
            .maxAge(maxAge)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public void expireRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie =
        ResponseCookie.from(properties.getName(), "")
            .httpOnly(true)
            .secure(properties.isSecure())
            .sameSite(properties.getSameSite())
            .path(properties.getPath())
            .maxAge(0)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public String extractRefreshToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (properties.getName().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
