package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores school-specific configuration settings as key-value pairs.
 * Values are stored as JSON strings for flexibility.
 */
@Entity
@Table(name = "school_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
}
