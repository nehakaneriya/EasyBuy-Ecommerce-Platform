package com.easy_buy.USER_SERVICE.repositories;

import com.easy_buy.USER_SERVICE.entity.RefreshToken;
import com.easy_buy.USER_SERVICE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByUser(User user);
}
