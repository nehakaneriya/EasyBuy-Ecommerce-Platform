package com.easy_buy.USER_SERVICE.controller;

import com.easy_buy.USER_SERVICE.dtos.request.LoginRequest;
import com.easy_buy.USER_SERVICE.dtos.request.RefreshTokenRequest;
import com.easy_buy.USER_SERVICE.dtos.request.RegisterRequest;
import com.easy_buy.USER_SERVICE.dtos.response.LoginResponse;
import com.easy_buy.USER_SERVICE.dtos.response.RefreshTokenResponse;
import com.easy_buy.USER_SERVICE.dtos.response.UserDto;
import com.easy_buy.USER_SERVICE.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        RefreshTokenResponse response = userService.refreshToken(refreshRequest);
        return ResponseEntity.ok(response);
    }
}
