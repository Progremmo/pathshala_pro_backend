package com.pathshalapro.service.impl;

import com.pathshalapro.dto.notification.AnnouncementRequest;
import com.pathshalapro.dto.notification.AnnouncementResponse;
import com.pathshalapro.dto.notification.NotificationRequest;
import com.pathshalapro.entity.*;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

    private final NotificationRepository notificationRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public Notification sendNotification(Long schoolId, NotificationRequest request, User sender) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        User recipient = null;
        if (request.getRecipientId() != null) {
            recipient = userRepository.findByIdAndIsDeletedFalse(request.getRecipientId())
                    .orElseThrow(() -> ApiException.notFound("Recipient not found."));
        }

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .notificationType(request.getNotificationType())
                .school(school)
                .recipient(recipient)
                .sender(sender)
                .scheduledAt(request.getScheduledAt())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .isSent(false)
                .isRead(false)
                .build();

        // If no scheduled time, send immediately
        if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now())) {
            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());
        }

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndIsDeletedFalse(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getRecipient() != null && n.getRecipient().getId().equals(userId) && !n.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Notification not found."));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUser(userId);
    }

    // ---- Announcements ----

    @Transactional
    public AnnouncementResponse createAnnouncement(Long schoolId, AnnouncementRequest request, User createdBy) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .targetAudience(request.getTargetAudience())
                .targetGrade(request.getTargetGrade())
                .isPinned(request.isPinned())
                .publishedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .attachmentUrl(request.getAttachmentUrl())
                .school(school)
                .createdByUser(createdBy)
                .build();

        return mapToAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long schoolId, Long announcementId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .filter(a -> a.getSchool().getId().equals(schoolId) && !a.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Announcement not found."));

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetAudience(request.getTargetAudience());
        announcement.setTargetGrade(request.getTargetGrade());
        announcement.setPinned(request.isPinned());
        announcement.setExpiresAt(request.getExpiresAt());
        announcement.setAttachmentUrl(request.getAttachmentUrl());

        return mapToAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteAnnouncement(Long schoolId, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .filter(a -> a.getSchool().getId().equals(schoolId) && !a.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Announcement not found."));
        announcement.setDeleted(true);
        announcementRepository.save(announcement);
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> getAnnouncementsBySchool(Long schoolId, String audience, Pageable pageable) {
        return announcementRepository.findActiveBySchoolAndAudience(
                schoolId, audience, LocalDateTime.now(), pageable).map(this::mapToAnnouncementResponse);
    }

    private AnnouncementResponse mapToAnnouncementResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .targetAudience(a.getTargetAudience())
                .targetGrade(a.getTargetGrade())
                .isPinned(a.isPinned())
                .publishedAt(a.getPublishedAt())
                .expiresAt(a.getExpiresAt())
                .attachmentUrl(a.getAttachmentUrl())
                .createdByUserId(a.getCreatedByUser().getId())
                .createdByUserName(a.getCreatedByUser().getFirstName() + " " + a.getCreatedByUser().getLastName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
