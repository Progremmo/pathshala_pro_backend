package com.pathshalapro.dto.exam;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MarksEntryRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    private Double marksObtained;

    private String grade;
    private String remarks;
    private boolean isAbsent = false;
}
