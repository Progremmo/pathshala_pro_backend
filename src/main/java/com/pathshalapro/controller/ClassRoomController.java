package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.school.ClassRoomRequest;
import com.pathshalapro.dto.school.ClassRoomResponse;
import com.pathshalapro.service.impl.ClassRoomServiceImpl;
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
@RequestMapping("/schools/{schoolId}/classrooms")
@RequiredArgsConstructor
@Tag(name = "Classroom Management", description = "Manage classrooms and sections")
public class ClassRoomController {

    private final ClassRoomServiceImpl classRoomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Get all classrooms for a school")
    public ResponseEntity<ApiResponse<List<ClassRoomResponse>>> getClassRooms(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(classRoomService.getClassRooms(schoolId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create a classroom")
    public ResponseEntity<ApiResponse<ClassRoomResponse>> createClassRoom(
            @PathVariable Long schoolId,
            @Valid @RequestBody ClassRoomRequest request) {
        ClassRoomResponse response = classRoomService.createClassRoom(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Classroom created successfully."));
    }

    @PutMapping("/{classRoomId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update a classroom")
    public ResponseEntity<ApiResponse<ClassRoomResponse>> updateClassRoom(
            @PathVariable Long schoolId,
            @PathVariable Long classRoomId,
            @Valid @RequestBody ClassRoomRequest request) {
        ClassRoomResponse response = classRoomService.updateClassRoom(schoolId, classRoomId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Classroom updated successfully."));
    }

    @DeleteMapping("/{classRoomId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Delete a classroom")
    public ResponseEntity<ApiResponse<Void>> deleteClassRoom(
            @PathVariable Long schoolId,
            @PathVariable Long classRoomId) {
        classRoomService.deleteClassRoom(schoolId, classRoomId);
        return ResponseEntity.ok(ApiResponse.success(null, "Classroom deleted successfully."));
    }
}
