package com.yechan.fishing.fishing_api.domain.auth.service;

import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;

public record AuthSessionResult(String refreshToken, AuthTokenResponse response) {}
