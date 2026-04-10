package com.yechan.fishing.fishing_api.domain.auth.entity.enums;

import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;

public enum AuthProvider {
  KAKAO,
  GOOGLE;

  public static AuthProvider from(String rawValue) {
    try {
      return AuthProvider.valueOf(rawValue.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new FishingException(ErrorCode.AUTH_PROVIDER_NOT_SUPPORTED);
    }
  }
}
