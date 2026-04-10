package com.yechan.fishing.fishing_api.domain.auth.entity;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserStatus;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_users_provider_user",
          columnNames = {"provider", "provider_user_id"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AuthProvider provider;

  @Column(name = "provider_user_id", nullable = false, length = 255)
  private String providerUserId;

  @Column(length = 255)
  private String email;

  @Column(nullable = false, length = 100)
  private String nickname;

  @Column(name = "profile_image_url", columnDefinition = "TEXT")
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Column(name = "report_count", nullable = false)
  private Integer reportCount = 0;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public static User create(
      AuthProvider provider,
      String providerUserId,
      String email,
      String nickname,
      String profileImageUrl,
      LocalDateTime now) {
    User user = new User();
    user.provider = provider;
    user.providerUserId = providerUserId;
    user.email = email;
    user.nickname = nickname;
    user.profileImageUrl = profileImageUrl;
    user.status = UserStatus.ACTIVE;
    user.role = UserRole.USER;
    user.reportCount = 0;
    user.lastLoginAt = now;
    user.createdAt = now;
    user.updatedAt = now;
    return user;
  }

  public void updateSocialProfile(
      String email, String nickname, String profileImageUrl, LocalDateTime now) {
    this.email = email;
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
    this.lastLoginAt = now;
    this.updatedAt = now;
  }

  public void ensureActive() {
    if (status != UserStatus.ACTIVE) {
      throw new FishingException(ErrorCode.AUTH_USER_INACTIVE);
    }
  }

  public void incrementReportCount() {
    reportCount = reportCount + 1;
  }
}
