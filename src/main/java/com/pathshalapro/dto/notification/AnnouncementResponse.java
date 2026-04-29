package com.pathshalapro.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private String targetAudience;
    private String targetGrade;
    private boolean isPinned;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private String attachmentUrl;
    
    private Long createdByUserId;
    private String createdByUserName;
    
    private LocalDateTime createdAt;
}
