package com.pathshalapro.dto.school;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SchoolRequest {

    @NotBlank(message = "School name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "School code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, digits, underscores, and hyphens")
    private String code;

    private String address;
    private String city;
    private String state;
    private String pincode;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String website;
    private String logoUrl;
}
