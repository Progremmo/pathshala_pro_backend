package com.pathshalapro.service.impl;

import com.pathshalapro.dto.auth.AuthResponse;
import com.pathshalapro.dto.auth.LoginRequest;
import com.pathshalapro.dto.auth.RegisterUserRequest;
import com.pathshalapro.dto.user.UserResponse;
import com.pathshalapro.entity.Role;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.OtpRepository;
import com.pathshalapro.repository.RoleRepository;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.repository.UserRepository;
import com.pathshalapro.security.JwtTokenProvider;
import com.pathshalapro.service.AuthService;
import com.pathshalapro.service.EmailService;
import com.pathshalapro.entity.Otp;
import com.pathshalapro.dto.auth.RegisterAdminRequest;
import com.pathshalapro.dto.auth.ForgotPasswordRequest;
import com.pathshalapro.dto.auth.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.server-url}")
    private String serverUrl;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        // Authenticate via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("User not found."));

        if (!user.isActive()) {
            throw ApiException.unauthorized("Your account is disabled. Please contact administrator.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("schoolId", user.getSchool() != null ? user.getSchool().getId() : "");
        extraClaims.put("roles", user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()));

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, extraClaims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        log.info("Login successful for: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        log.info("Registering user: {} with role: {}", request.getEmail(), request.getRole());

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered: " + request.getEmail());
        }

        // PROJECT_ADMIN doesn't need a school
        School school = null;
        if (request.getRole() != RoleName.PROJECT_ADMIN) {
            if (request.getSchoolId() == null) {
                throw ApiException.badRequest("School ID is required for role: " + request.getRole());
            }
            school = schoolRepository.findByIdAndIsDeletedFalse(request.getSchoolId())
                    .orElseThrow(() -> ApiException.notFound("School not found with ID: " + request.getSchoolId()));
        }

        // Get role entity
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> ApiException.notFound("Role not found: " + request.getRole()));

        // Handle password generation if missing
        String plainPassword = request.getPassword();
        if (plainPassword == null || plainPassword.isBlank()) {
            plainPassword = RandomStringUtils.randomAlphanumeric(8) + "1aA@";
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(plainPassword))
                .phone(request.getPhone())
                .school(school)
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .admissionNo(request.getAdmissionNo())
                .employeeId(request.getEmployeeId())
                .qualification(request.getQualification())
                .joiningDate(request.getJoiningDate())
                .isActive(true)
                .roles(new ArrayList<>(List.of(role)))
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: {}", saved.getId());

        // Send confirmation email
        String loginUrl = serverUrl + "/login";
        String subject = "Welcome to PathshalaPro - Registration Successful";
        String htmlBody = getRegistrationHtmlTemplate(request.getFirstName(), request.getEmail(), plainPassword,
                loginUrl);

        emailService.sendHtmlEmail(request.getEmail(), subject, htmlBody);

        return mapToUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtTokenProvider.isTokenValid(refreshToken, userDetails)) {
            throw ApiException.unauthorized("Invalid or expired refresh token.");
        }

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> ApiException.notFound("User not found."));

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public UserResponse registerAdmin(RegisterAdminRequest request) {
        log.info("Registering admin: {} for school: {}", request.getEmail(), request.getSchoolId());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered: " + request.getEmail());
        }

        School school = schoolRepository.findByIdAndIsDeletedFalse(request.getSchoolId())
                .orElseThrow(() -> ApiException.notFound("School not found with ID: " + request.getSchoolId()));

        Role role = roleRepository.findByName(RoleName.SCHOOL_ADMIN)
                .orElseThrow(() -> ApiException.notFound("Role not found: SCHOOL_ADMIN"));

        // Generate an 8-character random password
        String plainPassword = RandomStringUtils.randomAlphanumeric(8) + "1aA@"; // Ensure complexity

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(plainPassword))
                .phone(request.getPhone())
                .school(school)
                .isActive(true)
                .roles(new ArrayList<>(List.of(role)))
                .build();

        User saved = userRepository.save(user);

        // Send email
        String loginUrl = serverUrl + "/login";
        String subject = "Welcome to PathshalaPro - Admin Credentials";
        String htmlBody = getRegistrationHtmlTemplate(request.getFirstName(), request.getEmail(), plainPassword,
                loginUrl);

        emailService.sendHtmlEmail(request.getEmail(), subject, htmlBody);

        log.info("School Admin registered successfully: {}", saved.getId());
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("User not found."));

        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(request.getEmail());

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        Otp otp = Otp.builder()
                .email(request.getEmail())
                .otpCode(otpCode)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .build();

        otpRepository.save(otp);

        // Send email
        String subject = "PathshalaPro - Password Reset OTP";
        String body = String.format(
                "Hello %s,\n\nYour OTP for password reset is: %s\n\nThis OTP will expire in 10 minutes. If you didn't request this, please ignore this email.\n\nThanks,\nPathshalaPro Team",
                user.getFirstName(), otpCode);

        emailService.sendEmail(request.getEmail(), subject, body);
        log.info("Password reset OTP sent to: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Otp otp = otpRepository.findTopByEmailOrderByExpiryDateDesc(request.getEmail())
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired OTP."));

        if (otp.isUsed() || otp.getExpiryDate().isBefore(LocalDateTime.now())
                || !otp.getOtpCode().equals(request.getOtp())) {
            throw ApiException.badRequest("Invalid or expired OTP.");
        }

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("User not found."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);

        log.info("Password successfully reset for: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> ApiException.notFound("User not found."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw ApiException.badRequest("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    // ---- Helpers ----

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        List<RoleName> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .roles(roleNames)
                .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
                .schoolName(user.getSchool() != null ? user.getSchool().getName() : null)
                .classRoomId(user.getClassRoom() != null ? user.getClassRoom().getId() : null)
                .expiresIn(86400L)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.isActive())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
                .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
                .schoolName(user.getSchool() != null ? user.getSchool().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String getRegistrationHtmlTemplate(String name, String email, String password, String loginUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; }
                        .header { background: #4f46e5; color: #ffffff; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .content { padding: 30px; background: #ffffff; }
                        .credentials { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 20px; margin: 20px 0; }
                        .credentials p { margin: 5px 0; font-family: monospace; font-size: 14px; }
                        .button-container { text-align: center; margin-top: 30px; }
                        .button { background: #4f46e5; color: #ffffff !important; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; }
                        .footer { background: #f3f4f6; color: #6b7280; padding: 20px; text-align: center; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>PathshalaPro</h1>
                        </div>
                        <div class="content">
                            <h2 style="color: #4f46e5;">Welcome, %s!</h2>
                            <p>Your account has been successfully created. You can now access the PathshalaPro School Management System.</p>
                            <div class="credentials">
                                <p><strong>Email:</strong> %s</p>
                                <p><strong>Password:</strong> %s</p>
                            </div>
                            <p>Please log in and change your password immediately for better security.</p>
                            <div class="button-container">
                                <a href="%s" class="button">Login to Your Account</a>
                            </div>
                        </div>
                        <div class="footer">
                            &copy; 2026 PathshalaPro Team. All rights reserved.<br>
                            If you have any questions, contact us at support@pathshalapro.com
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(name, email, password, loginUrl);
    }
}
