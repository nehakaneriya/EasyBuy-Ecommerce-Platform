package com.easy_buy.USER_SERVICE.service.impl;

import com.easy_buy.USER_SERVICE.entity.RefreshToken;
import com.easy_buy.USER_SERVICE.entity.User;
import com.easy_buy.USER_SERVICE.exception.InvalidRequestException;
import com.easy_buy.USER_SERVICE.repositories.RefreshTokenRepository;
import com.easy_buy.USER_SERVICE.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RefreshToken createRefreshToken(User user, String refreshToken) {
        var refreshTokenOb = new RefreshToken();
        refreshTokenOb.setRefreshToken(refreshToken);
        refreshTokenOb.setActive(true);
        refreshTokenOb.setUser(user);
        refreshTokenOb.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        return refreshTokenRepository.save(refreshTokenOb);
    }

    @Override
    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken refreshTokenOb = refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid refresh token"));

        if (!refreshTokenOb.getActive()) {
            throw new InvalidRequestException("Invalid refresh token");
        }

        return refreshTokenOb;
    }

    @Override
    public void deactivateRefreshToken(RefreshToken refreshToken) {
        refreshToken.setActive(false);
        refreshTokenRepository.save(refreshToken);
    }
}
