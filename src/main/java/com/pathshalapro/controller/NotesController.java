package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.notes.NotesRequest;
import com.pathshalapro.dto.notes.NotesResponse;
import com.pathshalapro.security.SecurityUtils;
import com.pathshalapro.service.impl.NotesServiceImpl;
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
 * Notes and educational content controller.
 */
@RestController
@RequestMapping("/schools/{schoolId}/notes")
@RequiredArgsConstructor
@Tag(name = "Notes & Content", description = "Upload and manage subject-wise educational notes")
public class NotesController {

    private final NotesServiceImpl notesService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Upload notes/content", description = "Upload notes by providing a URL to the content file.")
    public ResponseEntity<ApiResponse<NotesResponse>> createNotes(
            @PathVariable Long schoolId,
            @Valid @RequestBody NotesRequest request) {
        NotesResponse notes = notesService.createNotes(schoolId, request, securityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(notes, "Notes uploaded."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all notes for a school")
    public ResponseEntity<ApiResponse<Page<NotesResponse>>> getNotesBySchool(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(notesService.getNotesBySchool(schoolId, pageable)));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get notes by subject")
    public ResponseEntity<ApiResponse<Page<NotesResponse>>> getNotesBySubject(
            @PathVariable Long schoolId,
            @PathVariable Long subjectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(notesService.getNotesBySubject(subjectId, schoolId, pageable)));
    }

    @PutMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Update notes")
    public ResponseEntity<ApiResponse<NotesResponse>> updateNotes(
            @PathVariable Long schoolId,
            @PathVariable Long noteId,
            @Valid @RequestBody NotesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(notesService.updateNotes(noteId, schoolId, request)));
    }

    @DeleteMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete notes")
    public ResponseEntity<ApiResponse<Void>> deleteNotes(
            @PathVariable Long schoolId,
            @PathVariable Long noteId) {
        notesService.deleteNotes(noteId, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notes deleted."));
    }
}
