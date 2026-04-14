package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the fee structure for a class within a school.
 * e.g. "Class 10 - Tuition Fee: ₹5000/month"
 */
@Entity
@Table(name = "fee_structures", indexes = {
    @Index(name = "idx_fee_structure_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name; // e.g. "Tuition Fee - Class 10"

    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType; // TUITION, TRANSPORT, HOSTEL, LIBRARY, EXAM, etc.

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "frequency", nullable = false, length = 20)
    private String frequency; // MONTHLY, QUARTERLY, ANNUALLY, ONE_TIME

    @Column(name = "grade", length = 20)
    private String grade; // Applicable grade

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_day")
    private Integer dueDay; // Day of month when payment is due

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "feeStructure", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeeInvoice> invoices = new ArrayList<>();
}
