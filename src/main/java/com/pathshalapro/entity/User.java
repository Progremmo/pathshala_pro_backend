package com.pathshalapro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User entity - supports all roles (student, teacher, admin, parent).
 * Multi-tenant: each user (except PROJECT_ADMIN) belongs to a school.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_school", columnList = "school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "profile_pic_url", length = 500)
    private String profilePicUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @Column(name = "employee_id", length = 50)
    private String employeeId; // For teachers/staff

    @Column(name = "admission_no", length = 50)
    private String admissionNo; // For students

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "qualification", length = 200)
    private String qualification; // For teachers

    @Column(name = "joining_date")
    private java.time.LocalDate joiningDate;

    // Multi-tenant relationship - nullable for PROJECT_ADMIN
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<StudentClassAllocation> classAllocations = new ArrayList<>();

    // Parent reference (for students)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<User> children = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();
}
