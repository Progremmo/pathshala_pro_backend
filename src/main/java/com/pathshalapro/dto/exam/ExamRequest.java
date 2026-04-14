package com.pathshalapro.dto.exam;

import com.pathshalapro.entity.enums.ExamType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ExamRequest {

    @NotBlank(message = "Exam name is required")
    private String name;

    @NotNull(message = "Exam type is required")
    private ExamType examType;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    private LocalTime startTime;
    private Integer durationMinutes;

    @NotNull(message = "Total marks is required")
    @DecimalMin(value = "1.0", message = "Total marks must be at least 1")
    private Double totalMarks;

    @NotNull(message = "Passing marks is required")
    @DecimalMin(value = "0.0", message = "Passing marks cannot be negative")
    private Double passingMarks;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    private String instructions;

    @NotNull(message = "Class room ID is required")
    private Long classRoomId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
}
