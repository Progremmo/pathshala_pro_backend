package com.pathshalapro.dto.fee;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeStructureRequest {

    @NotBlank(message = "Fee structure name is required")
    private String name;

    @NotBlank(message = "Fee type is required")
    private String feeType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    private String grade;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    private String description;
    private Integer dueDay;
}
