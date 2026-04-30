package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * School-wide announcement (visible to all relevant users).
 */
@Entity
@Table(name = "announcements", indexes = {
    @Index(name = "idx_ann_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "target_audience", length = 50)
    private String targetAudience; // ALL, STUDENT, TEACHER, PARENT

    @Column(name = "target_grade", length = 20)
    private String targetGrade; // Specific grade or null for all

    @Builder.Default
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user", nullable = false)
    private User createdByUser;
}
