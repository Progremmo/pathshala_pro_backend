package com.pathshalapro.service.impl;

import com.pathshalapro.entity.enums.AttendanceStatus;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Reports service - aggregates data for student performance, fee, and attendance reports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl {

    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final UserRepository userRepository;

    /**
     * Student performance report: attendance + marks summary.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentPerformanceReport(Long studentId,
                                                            Long classRoomId,
                                                            String academicYear,
                                                            LocalDate startDate,
                                                            LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Attendance stats
        Long totalDays = attendanceRepository.countTotalDaysByStudent(studentId, startDate, endDate);
        Long presentDays = attendanceRepository.countByStudentAndStatusAndDateRange(
                studentId, AttendanceStatus.PRESENT, startDate, endDate);
        Long lateDays = attendanceRepository.countByStudentAndStatusAndDateRange(
                studentId, AttendanceStatus.LATE, startDate, endDate);

        double attendancePct = totalDays > 0 ? (double) (presentDays + lateDays) / totalDays * 100 : 0;

        // Exam results
        var marksList = marksRepository.findStudentResultsByClassAndYear(studentId, classRoomId, academicYear);
        double avgMarks = marksList.stream()
                .filter(m -> !m.isAbsent() && m.getMarksObtained() != null)
                .mapToDouble(m -> (m.getMarksObtained() / m.getExam().getTotalMarks()) * 100)
                .average().orElse(0.0);

        report.put("studentId", studentId);
        report.put("attendancePercentage", Math.round(attendancePct * 100.0) / 100.0);
        report.put("totalDays", totalDays);
        report.put("presentDays", presentDays);
        report.put("lateDays", lateDays);
        report.put("examResults", marksList);
        report.put("averageScorePercentage", Math.round(avgMarks * 100.0) / 100.0);
        report.put("reportPeriod", Map.of("startDate", startDate, "endDate", endDate, "academicYear", academicYear));

        return report;
    }

    /**
     * Fee report for a school in a given year.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFeeReport(Long schoolId, Integer year) {
        BigDecimal totalCollected = feeInvoiceRepository.getTotalCollectedBySchoolAndYear(schoolId, year);
        BigDecimal totalOutstanding = feeInvoiceRepository.getTotalOutstandingBySchool(schoolId);

        Map<String, Object> report = new HashMap<>();
        report.put("schoolId", schoolId);
        report.put("year", year);
        report.put("totalCollected", totalCollected != null ? totalCollected : BigDecimal.ZERO);
        report.put("totalOutstanding", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        report.put("generatedAt", LocalDate.now());

        return report;
    }

    /**
     * Attendance report for a class in a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getClassAttendanceReport(Long schoolId, Long classRoomId,
                                                          LocalDate startDate, LocalDate endDate) {
        var records = attendanceRepository.findClassAttendance(schoolId, classRoomId, startDate, endDate);

        long totalRecords = records.size();
        long presentCount = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();

        return Map.of(
                "classRoomId", classRoomId,
                "startDate", startDate,
                "endDate", endDate,
                "totalRecords", totalRecords,
                "presentCount", presentCount,
                "absentCount", absentCount,
                "attendanceRate", totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0
        );
    }
}
