package com.yechan.fishing.fishing_api.domain.auth.jwt;

import java.time.LocalDateTime;

public record RefreshTokenPayload(Long userId, LocalDateTime expiresAt) {}
