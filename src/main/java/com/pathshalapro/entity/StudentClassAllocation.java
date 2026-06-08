package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mapping entity linking a student to a specific classroom for a given academic year.
 * Supports student promotions while retaining historical associations.
 */
@Entity
@Table(name = "student_class_allocations", indexes = {
    @Index(name = "idx_sca_student", columnList = "student_id"),
    @Index(name = "idx_sca_classroom", columnList = "class_room_id"),
    @Index(name = "idx_sca_year", columnList = "academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentClassAllocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
