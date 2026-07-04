package com.easy_buy.USER_SERVICE.service;

import com.easy_buy.USER_SERVICE.entity.RefreshToken;
import com.easy_buy.USER_SERVICE.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String refreshToken);

    RefreshToken verifyRefreshToken(String refreshToken);

    void deactivateRefreshToken(RefreshToken refreshToken);
}
