package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.service.impl.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Reports controller - generates student performance, fee, and attendance reports.
 */
@RestController
@RequestMapping("/schools/{schoolId}/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Generate student performance, fee, and attendance reports")
public class ReportController {

    private final ReportServiceImpl reportService;

    @GetMapping("/student/{studentId}/performance")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Student performance report",
               description = "Get comprehensive student report including attendance percentage and exam marks.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentPerformance(
            @PathVariable Long schoolId,
            @PathVariable Long studentId,
            @RequestParam Long classRoomId,
            @RequestParam String academicYear,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getStudentPerformanceReport(
                studentId, classRoomId, academicYear, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/fees")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Fee collection report for a year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeeReport(
            @PathVariable Long schoolId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getFeeReport(schoolId, year)));
    }

    @GetMapping("/attendance/class/{classRoomId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Class attendance report for a date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClassAttendanceReport(
            @PathVariable Long schoolId,
            @PathVariable Long classRoomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getClassAttendanceReport(schoolId, classRoomId, startDate, endDate)));
    }
}
