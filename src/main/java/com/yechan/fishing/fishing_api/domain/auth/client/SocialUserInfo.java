package com.yechan.fishing.fishing_api.domain.auth.client;

public record SocialUserInfo(
    String providerUserId, String email, String nickname, String profileImageUrl) {}
