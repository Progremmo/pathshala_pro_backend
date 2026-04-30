package com.pathshalapro.entity;

import com.pathshalapro.entity.enums.ExamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an exam scheduled for a class and subject.
 */
@Entity
@Table(name = "exams", indexes = {
    @Index(name = "idx_exam_school", columnList = "school_id"),
    @Index(name = "idx_exam_class", columnList = "class_room_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name; // e.g. "Mathematics Unit Test 1"

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 30)
    private ExamType examType;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_marks", nullable = false)
    private Double totalMarks;

    @Column(name = "passing_marks", nullable = false)
    private Double passingMarks;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Builder.Default
    @Column(name = "is_result_published", nullable = false)
    private boolean isResultPublished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Builder.Default
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Marks> marksList = new ArrayList<>();
}
