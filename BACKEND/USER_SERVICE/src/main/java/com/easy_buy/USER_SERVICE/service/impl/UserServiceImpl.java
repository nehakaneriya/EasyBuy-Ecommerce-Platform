package com.easy_buy.USER_SERVICE.service.impl;

import com.easy_buy.USER_SERVICE.dtos.request.ChangePasswordRequest;
import com.easy_buy.USER_SERVICE.dtos.request.RegisterRequest;
import com.easy_buy.USER_SERVICE.dtos.request.UpdateUserRequest;
import com.easy_buy.USER_SERVICE.dtos.response.PagedResponse;
import com.easy_buy.USER_SERVICE.dtos.response.UserDto;
import com.easy_buy.USER_SERVICE.entity.User;
import com.easy_buy.USER_SERVICE.enums.Role;
import com.easy_buy.USER_SERVICE.exception.EmailAlreadyExistsException;
import com.easy_buy.USER_SERVICE.exception.InvalidRequestException;
import com.easy_buy.USER_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.USER_SERVICE.repositories.UserRepository;
import com.easy_buy.USER_SERVICE.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // -------- User CRUD --------

    // Register a new user
    @Override
    public UserDto registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        user.setAddress(request.getAddress());

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    // Get all users with pagination
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return toPagedResponse(userPage);
    }

    // Get user by ID
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        return toDto(findUser(userId));
    }

    // Get user by email
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        return toDto(findUserByEmail(email));
    }

    @Override
    public UserDto updateUser(UUID userId, UpdateUserRequest request) {
        User user = findUser(userId);

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        return toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(UUID userId) {
        userRepository.delete(findUser(userId));
    }

    // -------- Profile Management --------

    @Override
    public UserDto changePassword(UUID userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("New password and confirm password do not match");
        }
        User user = findUser(userId);
        if (!request.getOldPassword().equals(user.getPassword())) {
            throw new InvalidRequestException("Old password is incorrect");
        }
        user.setPassword(request.getNewPassword());
        return toDto(userRepository.save(user));
    }

    @Override
    public UserDto updateProfileImage(UUID userId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new InvalidRequestException("Image URL cannot be empty");
        }
        User user = findUser(userId);
        user.setProfileImage(imageUrl);
        return toDto(userRepository.save(user));
    }

    @Override
    public UserDto changeUserRole(UUID userId, String roleName) {
        User user = findUser(userId);
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid role: " + roleName + ". Valid roles: ROLE_USER, ROLE_ADMIN");
        }
        return toDto(userRepository.save(user));
    }

    // -------- Private Helpers --------

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setProfileImage(user.getProfileImage());
        dto.setGender(user.getGender());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setAddress(user.getAddress());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private PagedResponse<UserDto> toPagedResponse(Page<User> page) {
        PagedResponse<UserDto> response = new PagedResponse<>();
        response.setContent(page.getContent().stream().map(this::toDto).toList());
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setNumberOfElements(page.getNumberOfElements());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}
