package com.easy_buy.USER_SERVICE.controller;

import com.easy_buy.USER_SERVICE.dtos.request.ChangePasswordRequest;
import com.easy_buy.USER_SERVICE.dtos.request.RegisterRequest;
import com.easy_buy.USER_SERVICE.dtos.request.UpdateUserRequest;
import com.easy_buy.USER_SERVICE.dtos.response.PagedResponse;
import com.easy_buy.USER_SERVICE.dtos.response.UserDto;
import com.easy_buy.USER_SERVICE.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    // GET /api/users - Get all users with pagination
    @GetMapping
    public ResponseEntity<PagedResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    // GET /api/users/{userId} - Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // GET /api/users/email/{email} - Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // PUT /api/users/{userId} - Update user details
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    // DELETE /api/users/{userId} - Delete user by ID
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/users/{userId}/change-password - Change user password
    @PatchMapping("/{userId}/change-password")
    public ResponseEntity<UserDto> changePassword(@PathVariable UUID userId,
                                                   @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(userId, request));
    }

    // PATCH /api/users/{userId}/profile-image - Update user profile image
    @PatchMapping("/{userId}/profile-image")
    public ResponseEntity<UserDto> updateProfileImage(@PathVariable UUID userId,
                                                       @RequestParam String imageUrl) {
        return ResponseEntity.ok(userService.updateProfileImage(userId, imageUrl));
    }

    // PATCH /api/users/{userId}/role - Change user role
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable UUID userId,
                                                   @RequestParam String role) {
        return ResponseEntity.ok(userService.changeUserRole(userId, role));
    }
}
