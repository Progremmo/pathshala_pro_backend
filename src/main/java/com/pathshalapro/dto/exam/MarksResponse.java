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
    private Double marksObtained;
    private String remarks;
    private Boolean isAbsent;
}
