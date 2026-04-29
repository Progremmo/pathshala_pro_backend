package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.school.SubjectRequest;
import com.pathshalapro.dto.school.SubjectResponse;
import com.pathshalapro.service.impl.SubjectServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schools/{schoolId}/subjects")
@RequiredArgsConstructor
@Tag(name = "Subject Management", description = "Manage school subjects")
public class SubjectController {

    private final SubjectServiceImpl subjectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all subjects for a school")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.getSubjects(schoolId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create a new subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @PathVariable Long schoolId,
            @Valid @RequestBody SubjectRequest request) {
        SubjectResponse subject = subjectService.createSubject(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subject, "Subject created successfully"));
    }

    @PutMapping("/{subjectId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update a subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long schoolId,
            @PathVariable Long subjectId,
            @Valid @RequestBody SubjectRequest request) {
        SubjectResponse subject = subjectService.updateSubject(schoolId, subjectId, request);
        return ResponseEntity.ok(ApiResponse.success(subject, "Subject updated successfully"));
    }

    @DeleteMapping("/{subjectId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Delete a subject")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable Long schoolId,
            @PathVariable Long subjectId) {
        subjectService.deleteSubject(schoolId, subjectId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subject deleted successfully"));
    }
}
