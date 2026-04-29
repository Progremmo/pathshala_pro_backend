package com.pathshalapro.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeStructureResponse {
    private Long id;
    private String name;
    private String feeType;
    private BigDecimal amount;
    private String frequency;
    private String grade;
    private String academicYear;
    private String description;
    private Integer dueDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
