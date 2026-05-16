package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.user.UserResponse;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Endpoints for managing and listing users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Get users by role and optional school")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @RequestParam(required = false) RoleName role,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<UserResponse> users = userService.getUsersByRoleAndSchool(role, schoolId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/classroom/{classRoomId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Get all students in a classroom")
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getStudentsByClass(@PathVariable Long classRoomId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getStudentsByClass(classRoomId)));
    }

    @GetMapping("/parent/{parentId}/children")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'PARENT')")
    @Operation(summary = "Get all children for a parent")
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getChildrenByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getChildrenByParentId(parentId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.pathshalapro.dto.user.UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info("Received status toggle request for user {}: {}", id, active);
        return ResponseEntity.ok(ApiResponse.success(userService.toggleStatus(id, active)));
    }
}
