package com.pathshalapro.dto.auth;

import com.pathshalapro.entity.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // If null, a secure password will be generated and emailed
    @Size(min = 8, max = 60, message = "Password must be between 8 and 60 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain at least one uppercase, one lowercase, and one digit")
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotNull(message = "Role is required")
    private RoleName role;

    // Required for school-level users (not PROJECT_ADMIN)
    private Long schoolId;

    // Optional: for students
    private Long classRoomId;
    private String admissionNo;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;

    // Optional: for teachers
    private String employeeId;
    private String qualification;
    private LocalDate joiningDate;

    // Optional: for students (link to parent user)
    private Long parentId;

    // Optional: for students (create new parent)
    private String parentFirstName;
    private String parentLastName;
    @Email(message = "Invalid parent email format")
    private String parentEmail;
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid parent phone number")
    private String parentPhone;
}
