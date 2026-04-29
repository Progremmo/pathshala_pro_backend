package com.pathshalapro.dto.exam;

import com.pathshalapro.entity.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {
    private Long id;
    private String name;
    private ExamType type;
    private LocalDate examDate;
    private LocalTime startTime;
    private Integer durationMinutes;
    private Double totalMarks;
    private Double passingMarks;
    private String academicYear;
    private boolean isPublished;
    private Long classRoomId;
    private String classRoomName;
    private Long subjectId;
    private String subjectName;
}
