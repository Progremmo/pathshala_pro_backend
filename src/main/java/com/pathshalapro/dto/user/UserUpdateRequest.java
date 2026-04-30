package com.pathshalapro.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s-]{10,20}$", message = "Invalid phone number")
    private String phone;

    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    @JsonProperty("isActive")
    @Getter(onMethod_ = {@JsonProperty("isActive")})
    @Setter(onMethod_ = {@JsonProperty("isActive")})
    private Boolean active;

    // Student specific
    private Long classRoomId;
    private String admissionNo;
    private Long parentId;

    // Teacher specific
    private String employeeId;
    private String qualification;
    private LocalDate joiningDate;
}
