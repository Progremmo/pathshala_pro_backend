package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores subject-wise notes/content for students.
 * Files are stored externally; only the URL is stored here.
 */
@Entity
@Table(name = "notes", indexes = {
    @Index(name = "idx_notes_school", columnList = "school_id"),
    @Index(name = "idx_notes_subject", columnList = "subject_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content_url", nullable = false, length = 500)
    private String contentUrl; // URL to the content file (PDF, video, etc.)

    @Column(name = "content_type", length = 50)
    private String contentType; // PDF, VIDEO, LINK, DOC

    @Column(name = "grade", length = 20)
    private String grade; // Applicable grade

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Builder.Default
    @Column(name = "is_visible", nullable = false)
    private boolean isVisible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Teacher who uploaded
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}
