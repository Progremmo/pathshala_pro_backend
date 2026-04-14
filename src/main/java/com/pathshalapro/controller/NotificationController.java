package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.notification.AnnouncementRequest;
import com.pathshalapro.dto.notification.NotificationRequest;
import com.pathshalapro.entity.Announcement;
import com.pathshalapro.entity.Notification;
import com.pathshalapro.security.SecurityUtils;
import com.pathshalapro.service.impl.NotificationServiceImpl;
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

import java.util.Map;

/**
 * Notifications and announcements controller.
 */
@RestController
@RequestMapping("/schools/{schoolId}/communication")
@RequiredArgsConstructor
@Tag(name = "Communication", description = "Send notifications and manage announcements")
public class NotificationController {

    private final NotificationServiceImpl notificationService;
    private final SecurityUtils securityUtils;

    // ---- Notifications ----

    @PostMapping("/notifications")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Send notification to a user or broadcast")
    public ResponseEntity<ApiResponse<Notification>> sendNotification(
            @PathVariable Long schoolId,
            @Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationService.sendNotification(schoolId, request, securityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(notification, "Notification sent."));
    }

    @GetMapping("/notifications/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's notifications")
    public ResponseEntity<ApiResponse<Page<Notification>>> getMyNotifications(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtils.getCurrentUser().getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId, pageable)));
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@PathVariable Long schoolId) {
        Long userId = securityUtils.getCurrentUser().getId();
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", notificationService.getUnreadCount(userId))));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long schoolId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId, securityUtils.getCurrentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read."));
    }

    @PatchMapping("/notifications/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long schoolId) {
        notificationService.markAllAsRead(securityUtils.getCurrentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read."));
    }

    // ---- Announcements ----

    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create a school-wide announcement")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(
            @PathVariable Long schoolId,
            @Valid @RequestBody AnnouncementRequest request) {
        Announcement announcement = notificationService.createAnnouncement(schoolId, request, securityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(announcement, "Announcement created."));
    }

    @GetMapping("/announcements")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active announcements for current user's role")
    public ResponseEntity<ApiResponse<Page<Announcement>>> getAnnouncements(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "ALL") String audience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getAnnouncementsBySchool(schoolId, audience, pageable)));
    }
}
