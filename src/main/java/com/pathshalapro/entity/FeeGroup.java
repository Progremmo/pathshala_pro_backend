package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of fee heads for easier assignment to classes/students.
 * e.g. "Class 10 - Monthly Fees"
 */
@Entity
@Table(name = "fee_groups", indexes = {
    @Index(name = "idx_fee_group_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "grade", length = 20)
    private String grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Builder.Default
    @OneToMany(mappedBy = "feeGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeeGroupItem> feeItems = new ArrayList<>();
}
