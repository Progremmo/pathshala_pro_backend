package com.pathshalapro.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalStudents;
    private long totalTeachers;
    private BigDecimal monthlyCollection;
    private BigDecimal monthlyPending;
    private double todayAttendancePercentage;
    
    private List<FeeTrendData> feeTrend;
    private List<AttendanceDistribution> attendanceDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeTrendData {
        private String month;
        private BigDecimal collected;
        private BigDecimal pending;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceDistribution {
        private String name;
        private long value;
        private String color;
    }
}
