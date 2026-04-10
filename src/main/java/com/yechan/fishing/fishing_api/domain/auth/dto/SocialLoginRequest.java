package com.yechan.fishing.fishing_api.domain.auth.dto;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SocialLoginRequest(
    @NotNull AuthProvider provider,
    @NotBlank String accessToken,
    @Size(max = 255) String deviceName) {}
