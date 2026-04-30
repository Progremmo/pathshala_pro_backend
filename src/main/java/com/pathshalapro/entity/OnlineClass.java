package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a scheduled online class (Agora-ready).
 * Stores meeting links and class schedule.
 */
@Entity
@Table(name = "online_classes", indexes = {
    @Index(name = "idx_oc_school", columnList = "school_id"),
    @Index(name = "idx_oc_teacher", columnList = "teacher_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineClass extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink; // Zoom/Google Meet/Agora channel URL

    @Column(name = "meeting_id", length = 100)
    private String meetingId; // Agora channel name or Zoom meeting ID

    @Column(name = "meeting_password", length = 100)
    private String meetingPassword;

    @Column(name = "platform", length = 50)
    private String platform; // AGORA, ZOOM, GOOGLE_MEET, TEAMS

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private boolean isRecurring = false;

    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern; // DAILY, WEEKLY, etc.

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "SCHEDULED"; // SCHEDULED, ONGOING, COMPLETED, CANCELLED

    @Column(name = "recording_url", length = 500)
    private String recordingUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
}
