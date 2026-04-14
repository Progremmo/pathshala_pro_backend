package com.pathshalapro.dto.notification;

import com.pathshalapro.entity.enums.NotificationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    private Long recipientId; // null = broadcast to all in school

    private LocalDateTime scheduledAt;

    private Long referenceId;
    private String referenceType;
}
