package com.pathshalapro.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeAllocationResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long classId;
    private String className;
    private String section;
    private Long studentId;
    private String studentName;
    private String academicYear;
    private LocalDateTime createdAt;
}
