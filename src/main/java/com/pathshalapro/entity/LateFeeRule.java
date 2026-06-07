package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Defines rules for applying late fees to overdue invoices.
 */
@Entity
@Table(name = "late_fee_rules", indexes = {
        @Index(name = "idx_late_rule_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LateFeeRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_type", nullable = false, length = 20)
    private String ruleType; // FIXED, PERCENTAGE, SLAB

    @Column(name = "rule_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal ruleValue;

    @Column(name = "grace_period_days", nullable = false)
    @Builder.Default
    private Integer gracePeriodDays = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
