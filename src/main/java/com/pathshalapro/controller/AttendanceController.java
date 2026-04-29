package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.attendance.AttendanceRequest;
import com.pathshalapro.dto.attendance.AttendanceResponse;
import com.pathshalapro.security.SecurityUtils;
import com.pathshalapro.service.impl.AttendanceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Attendance marking and retrieval controller.
 */
@RestController
@RequestMapping("/schools/{schoolId}/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Mark and retrieve daily student attendance")
public class AttendanceController {

    private final AttendanceServiceImpl attendanceService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Mark attendance for a class",
               description = "Bulk attendance marking for all students in a class on a given date.")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> markAttendance(
            @PathVariable Long schoolId,
            @Valid @RequestBody AttendanceRequest request) {
        List<AttendanceResponse> records = attendanceService.markAttendance(schoolId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(records,
                String.format("Attendance marked for %d students.", records.size())));
    }

    @GetMapping("/class/{classRoomId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Get attendance for a class on a date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getClassAttendance(
            @PathVariable Long schoolId,
            @PathVariable Long classRoomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getClassAttendance(schoolId, classRoomId, date)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get attendance records for a student in a date range")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getStudentAttendance(
            @PathVariable Long schoolId,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getStudentAttendance(studentId, startDate, endDate)));
    }

    @GetMapping("/student/{studentId}/stats")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get attendance statistics for a student")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceStats(
            @PathVariable Long schoolId,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendanceStats(studentId, startDate, endDate)));
    }
}
