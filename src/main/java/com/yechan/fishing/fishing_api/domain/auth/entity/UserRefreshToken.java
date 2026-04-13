package com.yechan.fishing.fishing_api.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
  private String refreshToken;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "revoked_at")
  private LocalDateTime revokedAt;

  @Column(name = "device_name", length = 255)
  private String deviceName;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  public static UserRefreshToken issue(
      User user,
      String refreshToken,
      LocalDateTime expiresAt,
      String deviceName,
      String userAgent,
      LocalDateTime now) {
    UserRefreshToken token = new UserRefreshToken();
    token.user = user;
    token.refreshToken = refreshToken;
    token.expiresAt = expiresAt;
    token.createdAt = now;
    token.updatedAt = now;
    token.deviceName = deviceName;
    token.userAgent = userAgent;
    return token;
  }

  public boolean isExpired(LocalDateTime now) {
    return expiresAt.isBefore(now);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public void revoke(LocalDateTime now) {
    this.revokedAt = now;
    this.updatedAt = now;
  }
}
