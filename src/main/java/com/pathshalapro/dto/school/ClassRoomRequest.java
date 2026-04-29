package com.pathshalapro.dto.school;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String section;

    @NotBlank(message = "Grade is required")
    private String grade;

    @NotBlank(message = "Academic Year is required")
    private String academicYear;

    private Integer capacity;

    private String roomNumber;

    private Long classTeacherId;
}
