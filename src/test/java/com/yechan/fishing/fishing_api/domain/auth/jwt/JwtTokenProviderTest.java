package com.yechan.fishing.fishing_api.domain.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

  @Test
  void issueTokens_andParseRefreshToken_returnsUserIdAndExpiry() {
    JwtProperties properties = new JwtProperties();
    properties.setIssuer("fishing-api");
    properties.setSecret("dev-secret-key-change-me-dev-secret-key-change-me");
    properties.setAccessTokenExpirationSeconds(1800);
    properties.setRefreshTokenExpirationSeconds(1209600);

    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(properties);
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    ReflectionTestUtils.setField(user, "id", 1L);

    IssuedTokens issuedTokens = jwtTokenProvider.issueTokens(user);
    RefreshTokenPayload payload = jwtTokenProvider.parseRefreshToken(issuedTokens.refreshToken());

    assertNotNull(issuedTokens.accessToken());
    assertNotNull(issuedTokens.refreshToken());
    assertEquals(1L, payload.userId());
    assertEquals(issuedTokens.refreshTokenExpiresAt().withNano(0), payload.expiresAt().withNano(0));
  }

  @Test
  void issueTokens_andParseAccessToken_returnsUserClaims() {
    JwtProperties properties = new JwtProperties();
    properties.setIssuer("fishing-api");
    properties.setSecret("dev-secret-key-change-me-dev-secret-key-change-me");
    properties.setAccessTokenExpirationSeconds(1800);
    properties.setRefreshTokenExpirationSeconds(1209600);

    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(properties);
    User user =
        User.create(
            AuthProvider.GOOGLE, "987654", "angler@example.com", "앵글러", null, LocalDateTime.now());
    ReflectionTestUtils.setField(user, "id", 7L);

    IssuedTokens issuedTokens = jwtTokenProvider.issueTokens(user);
    AccessTokenPayload payload = jwtTokenProvider.parseAccessToken(issuedTokens.accessToken());

    assertEquals(7L, payload.userId());
    assertEquals(UserRole.USER, payload.role());
    assertEquals(AuthProvider.GOOGLE, payload.provider());
    assertEquals(issuedTokens.accessTokenExpiresAt().withNano(0), payload.expiresAt().withNano(0));
  }
}
