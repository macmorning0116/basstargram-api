package com.yechan.fishing.fishing_api.domain.auth.jwt;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";
  private static final String OAUTH_STATE_TOKEN_TYPE = "oauth_state";
  private static final String USER_ID_CLAIM = "userId";
  private static final String ROLE_CLAIM = "role";
  private static final String PROVIDER_CLAIM = "provider";
  private static final long OAUTH_STATE_EXPIRATION_SECONDS = 300L;

  private final JwtProperties properties;
  private final Key signingKey;

  public JwtTokenProvider(JwtProperties properties) {
    this.properties = properties;
    this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public IssuedTokens issueTokens(User user) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime accessExpiresAt = now.plusSeconds(properties.getAccessTokenExpirationSeconds());
    LocalDateTime refreshExpiresAt = now.plusSeconds(properties.getRefreshTokenExpirationSeconds());

    String accessToken =
        Jwts.builder()
            .issuer(properties.getIssuer())
            .subject(String.valueOf(user.getId()))
            .claim(USER_ID_CLAIM, user.getId())
            .claim(ROLE_CLAIM, user.getRole().name())
            .claim(PROVIDER_CLAIM, user.getProvider().name())
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .issuedAt(toDate(now))
            .expiration(toDate(accessExpiresAt))
            .signWith(signingKey)
            .compact();

    String refreshToken =
        Jwts.builder()
            .issuer(properties.getIssuer())
            .subject(String.valueOf(user.getId()))
            .claim(USER_ID_CLAIM, user.getId())
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .issuedAt(toDate(now))
            .expiration(toDate(refreshExpiresAt))
            .signWith(signingKey)
            .compact();

    return new IssuedTokens(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
  }

  public RefreshTokenPayload parseRefreshToken(String refreshToken) {
    Claims claims = parseClaims(refreshToken, ErrorCode.AUTH_INVALID_REFRESH_TOKEN).getPayload();
    String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
    if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
      throw new FishingException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    Long userId = getLongClaim(claims, USER_ID_CLAIM, ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    Date expiration = claims.getExpiration();
    if (userId == null || expiration == null) {
      throw new FishingException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    return new RefreshTokenPayload(
        userId, LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault()));
  }

  public String issueOAuthState(AuthProvider provider) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusSeconds(OAUTH_STATE_EXPIRATION_SECONDS);

    return Jwts.builder()
        .issuer(properties.getIssuer())
        .claim(PROVIDER_CLAIM, provider.name())
        .claim(TOKEN_TYPE_CLAIM, OAUTH_STATE_TOKEN_TYPE)
        .issuedAt(toDate(now))
        .expiration(toDate(expiresAt))
        .signWith(signingKey)
        .compact();
  }

  public void validateOAuthState(AuthProvider provider, String state) {
    Claims claims = parseClaims(state, ErrorCode.AUTH_INVALID_OAUTH_STATE).getPayload();
    String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
    String providerValue = claims.get(PROVIDER_CLAIM, String.class);
    if (!OAUTH_STATE_TOKEN_TYPE.equals(tokenType) || providerValue == null) {
      throw new FishingException(ErrorCode.AUTH_INVALID_OAUTH_STATE);
    }

    try {
      if (AuthProvider.valueOf(providerValue) != provider) {
        throw new FishingException(ErrorCode.AUTH_INVALID_OAUTH_STATE);
      }
    } catch (IllegalArgumentException e) {
      throw new FishingException(ErrorCode.AUTH_INVALID_OAUTH_STATE);
    }
  }

  public AccessTokenPayload parseAccessToken(String accessToken) {
    Claims claims = parseClaims(accessToken, ErrorCode.AUTH_INVALID_TOKEN).getPayload();
    String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
    if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
      throw new FishingException(ErrorCode.AUTH_INVALID_TOKEN);
    }

    Long userId = getLongClaim(claims, USER_ID_CLAIM, ErrorCode.AUTH_INVALID_TOKEN);
    String roleValue = claims.get(ROLE_CLAIM, String.class);
    String providerValue = claims.get(PROVIDER_CLAIM, String.class);
    Date expiration = claims.getExpiration();
    if (userId == null || roleValue == null || providerValue == null || expiration == null) {
      throw new FishingException(ErrorCode.AUTH_INVALID_TOKEN);
    }

    try {
      return new AccessTokenPayload(
          userId,
          UserRole.valueOf(roleValue),
          AuthProvider.valueOf(providerValue),
          LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault()));
    } catch (IllegalArgumentException e) {
      throw new FishingException(ErrorCode.AUTH_INVALID_TOKEN);
    }
  }

  private Jws<Claims> parseClaims(String token, ErrorCode errorCode) {
    try {
      return Jwts.parser()
          .verifyWith((javax.crypto.SecretKey) signingKey)
          .build()
          .parseSignedClaims(token);
    } catch (Exception e) {
      throw new FishingException(errorCode);
    }
  }

  private Long getLongClaim(Claims claims, String claimName, ErrorCode errorCode) {
    Object value = claims.get(claimName);
    if (value instanceof Number number) {
      return number.longValue();
    }
    throw new FishingException(errorCode);
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
