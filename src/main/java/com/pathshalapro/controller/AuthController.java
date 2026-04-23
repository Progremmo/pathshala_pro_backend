package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.auth.AuthResponse;
import com.pathshalapro.dto.auth.LoginRequest;
import com.pathshalapro.dto.auth.RegisterUserRequest;
import com.pathshalapro.dto.auth.RegisterAdminRequest;
import com.pathshalapro.dto.auth.ForgotPasswordRequest;
import com.pathshalapro.dto.auth.ResetPasswordRequest;
import com.pathshalapro.dto.user.UserResponse;
import com.pathshalapro.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller - handles login, registration, and token refresh.
 * All endpoints are public (no JWT required).
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, register, and manage JWT tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email and password. Returns JWT access and refresh tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user. PROJECT_ADMIN can register all roles. SCHOOL_ADMIN can register TEACHER/STUDENT/PARENT.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "User registered successfully."));
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Register a school admin", description = "Registers a school admin, auto-generates a secure password, and emails it.")
    public ResponseEntity<ApiResponse<UserResponse>> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        UserResponse response = authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Admin registered successfully. Credentials sent via email."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Generates a 6-digit OTP and sends it to the user's email.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "If the email is registered, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user's password using the OTP.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password successfully reset."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get a new access token using a valid refresh token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Refresh token is required."));
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the current user's password.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam Long userId,
            @RequestBody Map<String, String> body) {
        authService.changePassword(userId, body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }
}
