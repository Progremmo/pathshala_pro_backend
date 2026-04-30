package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Marks entry for a student in a specific exam.
 */
@Entity
@Table(name = "marks", indexes = {
    @Index(name = "idx_marks_exam", columnList = "exam_id"),
    @Index(name = "idx_marks_student", columnList = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marks extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "grade", length = 5)
    private String grade; // A+, A, B+, B, C ...

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Builder.Default
    @Column(name = "is_absent", nullable = false)
    private boolean isAbsent = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // Teacher who entered marks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private User enteredBy;
}
