package com.pathshalapro.service;

import com.pathshalapro.dto.auth.AuthResponse;
import com.pathshalapro.dto.auth.LoginRequest;
import com.pathshalapro.dto.auth.RegisterUserRequest;
import com.pathshalapro.dto.user.UserResponse;

/**
 * Authentication service interface.
 */
public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterUserRequest request);
    AuthResponse refreshToken(String refreshToken);
    void changePassword(Long userId, String currentPassword, String newPassword);
}
