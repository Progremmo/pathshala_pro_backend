package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a specific fee head/type.
 * e.g. "Tuition Fee", "Library Fee", "Transport Fee"
 */
@Entity
@Table(name = "fee_heads", indexes = {
        @Index(name = "idx_fee_head_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeHead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private boolean isMandatory = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
