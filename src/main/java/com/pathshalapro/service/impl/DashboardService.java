package com.pathshalapro.service.impl;

import com.pathshalapro.dto.dashboard.DashboardStatsResponse;
import com.pathshalapro.entity.enums.AttendanceStatus;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.repository.AttendanceRepository;
import com.pathshalapro.repository.FeeInvoiceRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getSchoolStats(Long schoolId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // Basic Counts
        long totalStudents = userRepository.countBySchoolIdAndRoleName(schoolId, RoleName.STUDENT);
        long totalTeachers = userRepository.countBySchoolIdAndRoleName(schoolId, RoleName.TEACHER);

        // Fees
        BigDecimal monthlyCollection = feeInvoiceRepository.getCollectionSince(schoolId, startOfMonth.atStartOfDay());
        BigDecimal monthlyPending = feeInvoiceRepository.getOutstandingSince(schoolId, startOfMonth);

        // Attendance Today
        long totalAttendanceMarked = attendanceRepository.countBySchoolIdAndDate(schoolId, today);
        long presentToday = attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today, AttendanceStatus.PRESENT);
        long lateToday = attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today, AttendanceStatus.LATE);
        
        double attendancePct = totalAttendanceMarked > 0 ? (double) (presentToday + lateToday) / totalAttendanceMarked * 100 : 0;

        // Fee Trend (Last 4 months)
        List<DashboardStatsResponse.FeeTrendData> feeTrend = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            LocalDate start = monthDate.withDayOfMonth(1);
            
            BigDecimal coll = feeInvoiceRepository.getCollectionSince(schoolId, start.atStartOfDay());
            BigDecimal pend = feeInvoiceRepository.getOutstandingSince(schoolId, start);
            
            feeTrend.add(DashboardStatsResponse.FeeTrendData.builder()
                    .month(monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .collected(coll != null ? coll : BigDecimal.ZERO)
                    .pending(pend != null ? pend : BigDecimal.ZERO)
                    .build());
        }

        // Attendance Distribution Today
        List<DashboardStatsResponse.AttendanceDistribution> distribution = new ArrayList<>();
        distribution.add(new DashboardStatsResponse.AttendanceDistribution("Present", presentToday, "#10b981"));
        distribution.add(new DashboardStatsResponse.AttendanceDistribution("Absent", 
                attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today, AttendanceStatus.ABSENT), "#ef4444"));
        distribution.add(new DashboardStatsResponse.AttendanceDistribution("Late", lateToday, "#f59e0b"));
        distribution.add(new DashboardStatsResponse.AttendanceDistribution("Leave", 
                attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today, AttendanceStatus.LEAVE), "#8b5cf6"));

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .monthlyCollection(monthlyCollection != null ? monthlyCollection : BigDecimal.ZERO)
                .monthlyPending(monthlyPending != null ? monthlyPending : BigDecimal.ZERO)
                .todayAttendancePercentage(Math.round(attendancePct * 10.0) / 10.0)
                .feeTrend(feeTrend)
                .attendanceDistribution(distribution)
                .build();
    }
}
