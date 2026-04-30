package com.pathshalapro.dto.user;

import com.pathshalapro.entity.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User response DTO - never exposes password or sensitive fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String profilePicUrl;
    @JsonProperty("isActive")
    private boolean active;

    @JsonProperty("isEmailVerified")
    private boolean emailVerified;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String admissionNo;
    private String employeeId;
    private String qualification;
    private LocalDate joiningDate;
    private List<RoleName> roles;
    private Long schoolId;
    private String schoolName;
    private Long classRoomId;
    private String classRoomName;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdAt;
}
