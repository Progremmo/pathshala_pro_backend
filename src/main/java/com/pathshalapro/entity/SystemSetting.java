package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores platform-wide configuration settings as key-value pairs.
 * Managed by PROJECT_ADMIN.
 */
@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_group")
    private String configGroup; // e.g., "GENERAL", "EMAIL", "SECURITY"

    @Column(name = "description")
    private String description;
}
