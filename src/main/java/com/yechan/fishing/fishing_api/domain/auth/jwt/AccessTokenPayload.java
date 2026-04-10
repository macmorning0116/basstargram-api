package com.yechan.fishing.fishing_api.domain.auth.jwt;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;
import java.time.LocalDateTime;

public record AccessTokenPayload(
    Long userId, UserRole role, AuthProvider provider, LocalDateTime expiresAt) {}
