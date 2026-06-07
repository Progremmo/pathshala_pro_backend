package com.pathshalapro.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long examId;
    private String examName;
    private String examTitle;
    private String subjectName;
    private String examType;
    private String academicYear;
    private java.time.LocalDate examDate;
    private Double marksObtained;
    private Double maxMarks;
    private String grade;
    private String remarks;
    private Boolean isAbsent;
}
