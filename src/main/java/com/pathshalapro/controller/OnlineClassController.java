package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.onlineclass.OnlineClassRequest;
import com.pathshalapro.entity.OnlineClass;
import com.pathshalapro.service.impl.OnlineClassServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Online class scheduling controller (Agora-ready).
 */
@RestController
@RequestMapping("/schools/{schoolId}/online-classes")
@RequiredArgsConstructor
@Tag(name = "Online Classes", description = "Schedule and manage online classes (Agora/Zoom ready)")
public class OnlineClassController {

    private final OnlineClassServiceImpl onlineClassService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Schedule an online class")
    public ResponseEntity<ApiResponse<OnlineClass>> scheduleClass(
            @PathVariable Long schoolId,
            @Valid @RequestBody OnlineClassRequest request) {
        OnlineClass oc = onlineClassService.scheduleClass(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(oc, "Online class scheduled."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all online classes for a school")
    public ResponseEntity<ApiResponse<Page<OnlineClass>>> getClasses(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        return ResponseEntity.ok(ApiResponse.success(onlineClassService.getClassesBySchool(schoolId, pageable)));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get upcoming online classes (next N days)")
    public ResponseEntity<ApiResponse<List<OnlineClass>>> getUpcoming(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(onlineClassService.getUpcomingClasses(schoolId, days)));
    }

    @PatchMapping("/{classId}/status")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Update class status (SCHEDULED/ONGOING/COMPLETED/CANCELLED)")
    public ResponseEntity<ApiResponse<OnlineClass>> updateStatus(
            @PathVariable Long schoolId,
            @PathVariable Long classId,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(onlineClassService.updateStatus(schoolId, classId, status)));
    }

    @DeleteMapping("/{classId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Cancel/delete an online class")
    public ResponseEntity<ApiResponse<Void>> deleteClass(
            @PathVariable Long schoolId,
            @PathVariable Long classId) {
        onlineClassService.deleteClass(schoolId, classId);
        return ResponseEntity.ok(ApiResponse.success(null, "Online class cancelled."));
    }
}
