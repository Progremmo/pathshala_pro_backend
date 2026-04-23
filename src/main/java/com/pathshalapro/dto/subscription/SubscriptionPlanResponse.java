package com.pathshalapro.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal priceMonthly;
    private BigDecimal priceAnnually;
    private Integer maxStudents;
    private Integer maxTeachers;
    private Integer maxClasses;
    private Integer storageGb;
    private List<String> features;
    private boolean isActive;
}
