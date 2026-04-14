package com.pathshalapro.dto.notification;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String targetAudience = "ALL"; // ALL, STUDENT, TEACHER, PARENT

    private String targetGrade;
    private boolean isPinned = false;
    private LocalDateTime expiresAt;
    private String attachmentUrl;
}
