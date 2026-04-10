package com.yechan.fishing.fishing_api.domain.auth.client;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;

public interface SocialUserInfoClient {

  AuthProvider provider();

  SocialUserInfo getUserInfo(String accessToken);
}
