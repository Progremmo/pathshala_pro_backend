package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a plan for breaking down an annual fee into installments.
 */
@Entity
@Table(name = "fee_installment_plans", indexes = {
        @Index(name = "idx_fip_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeInstallmentPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Builder.Default
    @OneToMany(mappedBy = "feeInstallmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeeInstallment> installments = new ArrayList<>();
}
