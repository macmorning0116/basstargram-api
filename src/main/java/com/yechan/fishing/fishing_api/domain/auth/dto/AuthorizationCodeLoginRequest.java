package com.yechan.fishing.fishing_api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorizationCodeLoginRequest(
    @NotBlank String code, @NotBlank String state, @Size(max = 255) String deviceName) {}
