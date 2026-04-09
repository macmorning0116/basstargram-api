package com.yechan.fishing.fishing_api.domain.auth.repository;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByProviderAndProviderUserId(String provider, String providerUserId);
}
