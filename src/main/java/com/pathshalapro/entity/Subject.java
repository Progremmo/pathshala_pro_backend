package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject entity - linked to school and can appear in timetables, notes, exams.
 */
@Entity
@Table(name = "subjects", indexes = {
    @Index(name = "idx_subject_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name; // e.g. "Mathematics"

    @Column(name = "code", nullable = false, length = 20)
    private String code; // e.g. "MATH101"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "grade", length = 20)
    private String grade; // Applicable grade level

    @Column(name = "credit_hours")
    private Integer creditHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notes> notesList = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Timetable> timetables = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Exam> exams = new ArrayList<>();
}
