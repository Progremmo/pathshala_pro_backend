package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Defines a specific installment within an installment plan.
 */
@Entity
@Table(name = "fee_installments", indexes = {
        @Index(name = "idx_installment_plan", columnList = "fee_installment_plan_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeInstallment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber; // 1, 2, 3, etc.

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_installment_plan_id", nullable = false)
    private FeeInstallmentPlan feeInstallmentPlan;
}
