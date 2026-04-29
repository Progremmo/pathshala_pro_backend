package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.timetable.TimetableRequest;
import com.pathshalapro.dto.timetable.TimetableResponse;
import com.pathshalapro.service.impl.TimetableServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Timetable CRUD controller with conflict detection.
 */
@RestController
@RequestMapping("/schools/{schoolId}/timetable")
@RequiredArgsConstructor
@Tag(name = "Timetable", description = "Manage class and teacher schedules with conflict detection")
public class TimetableController {

    private final TimetableServiceImpl timetableService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create timetable entry", description = "Creates a timetable slot. Detects and rejects scheduling conflicts.")
    public ResponseEntity<ApiResponse<TimetableResponse>> createEntry(
            @PathVariable Long schoolId,
            @Valid @RequestBody TimetableRequest request) {
        TimetableResponse entry = timetableService.createEntry(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(entry, "Timetable entry created."));
    }

    @PutMapping("/{entryId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update timetable entry")
    public ResponseEntity<ApiResponse<TimetableResponse>> updateEntry(
            @PathVariable Long schoolId,
            @PathVariable Long entryId,
            @Valid @RequestBody TimetableRequest request) {
        return ResponseEntity.ok(ApiResponse.success(timetableService.updateEntry(schoolId, entryId, request)));
    }

    @GetMapping("/class/{classRoomId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get timetable for a classroom")
    public ResponseEntity<ApiResponse<List<TimetableResponse>>> getClassTimetable(
            @PathVariable Long schoolId,
            @PathVariable Long classRoomId,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(timetableService.getClassTimetable(classRoomId, academicYear)));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Get timetable for a teacher")
    public ResponseEntity<ApiResponse<List<TimetableResponse>>> getTeacherTimetable(
            @PathVariable Long schoolId,
            @PathVariable Long teacherId,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(timetableService.getTeacherTimetable(teacherId, academicYear)));
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Delete timetable entry")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(
            @PathVariable Long schoolId,
            @PathVariable Long entryId) {
        timetableService.deleteEntry(schoolId, entryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timetable entry deleted."));
    }
}
