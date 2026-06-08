package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a classroom/section within a school.
 * e.g. "Class 10 - Section A"
 */
@Entity
@Table(name = "class_rooms", indexes = {
    @Index(name = "idx_classroom_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g. "Class 10"

    @Column(name = "section", length = 10)
    private String section; // e.g. "A", "B"

    @Column(name = "grade", nullable = false, length = 20)
    private String grade; // e.g. "10", "11", "12"

    @Column(name = "academic_year", length = 20)
    private String academicYear; // e.g. "2024-25"

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // Class teacher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private User classTeacher;

    @Builder.Default
    @OneToMany(mappedBy = "classRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentClassAllocation> allocations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "classRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Timetable> timetables = new ArrayList<>();
}
