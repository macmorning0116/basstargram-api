package com.yechan.fishing.fishing_api.domain.auth.jwt;

import java.time.LocalDateTime;

public record IssuedTokens(
    String accessToken,
    String refreshToken,
    LocalDateTime accessTokenExpiresAt,
    LocalDateTime refreshTokenExpiresAt) {}
