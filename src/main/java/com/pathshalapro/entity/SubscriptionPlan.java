package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Defines available subscription plans (SaaS plan tiers).
 * e.g. BASIC (up to 500 students), PRO, ENTERPRISE
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name; // BASIC, PRO, ENTERPRISE

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    @Column(name = "price_annually", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAnnually;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "max_teachers")
    private Integer maxTeachers;

    @Column(name = "max_classes")
    private Integer maxClasses;

    @Column(name = "storage_gb")
    private Integer storageGb;

    @Column(name = "features", columnDefinition = "JSON")
    private String features; // JSON array of feature flags

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
