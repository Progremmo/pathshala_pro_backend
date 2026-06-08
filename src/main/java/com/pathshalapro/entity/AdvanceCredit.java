package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tracks advance payments made by a parent/student.
 */
@Entity
@Table(name = "advance_credits", indexes = {
        @Index(name = "idx_advance_student", columnList = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvanceCredit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private User student;
}
