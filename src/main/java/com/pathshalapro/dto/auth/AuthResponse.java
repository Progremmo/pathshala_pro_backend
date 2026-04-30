package com.pathshalapro.dto.auth;

import com.pathshalapro.entity.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private List<RoleName> roles;
    private Long schoolId;
    private String schoolName;
    private Long expiresIn; // in seconds
}
