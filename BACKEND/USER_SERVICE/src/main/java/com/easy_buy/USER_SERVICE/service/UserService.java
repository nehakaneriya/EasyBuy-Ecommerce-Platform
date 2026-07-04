package com.easy_buy.USER_SERVICE.service;

import com.easy_buy.USER_SERVICE.dtos.request.ChangePasswordRequest;
import com.easy_buy.USER_SERVICE.dtos.request.LoginRequest;
import com.easy_buy.USER_SERVICE.dtos.request.RefreshTokenRequest;
import com.easy_buy.USER_SERVICE.dtos.request.RegisterRequest;
import com.easy_buy.USER_SERVICE.dtos.request.UpdateUserRequest;
import com.easy_buy.USER_SERVICE.dtos.response.LoginResponse;
import com.easy_buy.USER_SERVICE.dtos.response.PagedResponse;
import com.easy_buy.USER_SERVICE.dtos.response.RefreshTokenResponse;
import com.easy_buy.USER_SERVICE.dtos.response.UserDto;

import java.util.UUID;

public interface UserService {

    // User CRUD
    UserDto registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    PagedResponse<UserDto> getAllUsers(int page, int size);

    UserDto getUserById(UUID userId);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UUID userId, UpdateUserRequest request);

    void deleteUser(UUID userId);

    // Profile management
    UserDto changePassword(UUID userId, ChangePasswordRequest request);

    UserDto updateProfileImage(UUID userId, String imageUrl);

    // Admin
    UserDto changeUserRole(UUID userId, String role);
}
