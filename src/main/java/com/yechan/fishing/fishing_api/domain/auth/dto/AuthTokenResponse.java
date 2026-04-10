package com.yechan.fishing.fishing_api.domain.auth.dto;

import java.time.LocalDateTime;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    LocalDateTime accessTokenExpiresAt,
    LocalDateTime refreshTokenExpiresAt,
    AuthUserResponse user) {}
