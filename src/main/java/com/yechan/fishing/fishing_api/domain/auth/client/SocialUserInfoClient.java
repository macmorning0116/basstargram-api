package com.yechan.fishing.fishing_api.domain.auth.client;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;

public interface SocialUserInfoClient {

  AuthProvider provider();

  String buildAuthorizationUrl(String state);

  String exchangeCode(String code);

  SocialUserInfo getUserInfo(String accessToken);
}
