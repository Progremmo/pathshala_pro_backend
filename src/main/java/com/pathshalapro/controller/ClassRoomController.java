package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.school.ClassRoomResponse;
import com.pathshalapro.service.impl.ClassRoomServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
