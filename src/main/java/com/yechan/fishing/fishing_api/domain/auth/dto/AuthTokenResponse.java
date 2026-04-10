package com.yechan.fishing.fishing_api.domain.auth.dto;

import java.time.LocalDateTime;

public record AuthTokenResponse(
    String accessToken,
    LocalDateTime accessTokenExpiresAt,
    LocalDateTime refreshTokenExpiresAt,
    AuthUserResponse user) {}
