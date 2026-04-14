package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.school.SchoolRequest;
import com.pathshalapro.dto.school.SchoolResponse;
import com.pathshalapro.service.impl.SchoolServiceImpl;
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

/**
 * School management controller.
 * Only PROJECT_ADMIN can create/delete schools.
 */
@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
@Tag(name = "School Management", description = "Manage schools in the multi-tenant system")
public class SchoolController {

    private final SchoolServiceImpl schoolService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Create a school", description = "Create a new school. Only PROJECT_ADMIN can create schools.")
    public ResponseEntity<ApiResponse<SchoolResponse>> createSchool(@Valid @RequestBody SchoolRequest request) {
        SchoolResponse response = schoolService.createSchool(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "School created successfully."));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "List all schools", description = "Get all schools. Only accessible to PROJECT_ADMIN.")
    public ResponseEntity<ApiResponse<Page<SchoolResponse>>> getAllSchools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<SchoolResponse> schools = schoolService.getAllSchools(pageable);
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @GetMapping("/{schoolId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Get school by ID")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.getSchoolById(schoolId)));
    }

    @PutMapping("/{schoolId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update school details")
    public ResponseEntity<ApiResponse<SchoolResponse>> updateSchool(
            @PathVariable Long schoolId,
            @Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.updateSchool(schoolId, request), "School updated."));
    }

    @DeleteMapping("/{schoolId}")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Delete school", description = "Soft-deletes a school and all associated data.")
    public ResponseEntity<ApiResponse<Void>> deleteSchool(@PathVariable Long schoolId) {
        schoolService.deleteSchool(schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "School deleted successfully."));
    }
}
