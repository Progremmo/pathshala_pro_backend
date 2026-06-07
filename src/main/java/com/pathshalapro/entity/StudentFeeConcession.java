package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Defines a discount/scholarship/concession for a specific student.
 */
@Entity
@Table(name = "student_fee_concessions", indexes = {
        @Index(name = "idx_concession_student", columnList = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFeeConcession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // FLAT, PERCENTAGE

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "reason", length = 100)
    private String reason; // e.g. Sibling, Staff, Merit

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_head_id")
    private FeeHead feeHead; // If null, applies to the total invoice amount
}
