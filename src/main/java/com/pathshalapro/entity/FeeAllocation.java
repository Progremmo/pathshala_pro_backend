package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Assigns a FeeGroup to a ClassRoom or a specific Student.
 */
@Entity
@Table(name = "fee_allocations", indexes = {
    @Index(name = "idx_fee_alloc_school", columnList = "school_id"),
    @Index(name = "idx_fee_alloc_class", columnList = "class_room_id"),
    @Index(name = "idx_fee_alloc_student", columnList = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeAllocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_group_id", nullable = false)
    private FeeGroup feeGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id")
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
