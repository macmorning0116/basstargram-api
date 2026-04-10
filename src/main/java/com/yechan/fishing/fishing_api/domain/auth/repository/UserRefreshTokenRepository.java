package com.yechan.fishing.fishing_api.domain.auth.repository;

import com.yechan.fishing.fishing_api.domain.auth.entity.UserRefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

  Optional<UserRefreshToken> findByRefreshTokenAndRevokedAtIsNull(String refreshToken);
}
