package com.pathshalapro.config;

import com.pathshalapro.entity.Role;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.SubscriptionPlan;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.entity.enums.SubscriptionStatus;
import com.pathshalapro.repository.RoleRepository;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.repository.SubscriptionPlanRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data seeder - runs on application startup.
 * Seeds roles, subscription plans, and a default PROJECT_ADMIN user.
 * Only runs in 'default' and 'dev' profiles (not in production tests).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile({"default", "dev", "prod"})
    public CommandLineRunner seedData() {
        return args -> {
            log.info("==== PathshalaPro Data Seeder Starting ====");
            seedRoles();
            seedSubscriptionPlans();
            seedProjectAdmin();
            seedDemoSchool();
            log.info("==== Data Seeder Completed ====");
        };
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getRoleDescription(roleName))
                        .build();
                roleRepository.save(role);
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedSubscriptionPlans() {
        if (planRepository.count() == 0) {
            List<SubscriptionPlan> plans = List.of(
                SubscriptionPlan.builder()
                    .name("STARTER")
                    .description("Perfect for small schools. Up to 200 students.")
                    .priceMonthly(new BigDecimal("999.00"))
                    .priceAnnually(new BigDecimal("9999.00"))
                    .maxStudents(200)
                    .maxTeachers(20)
                    .maxClasses(10)
                    .storageGb(5)
                    .isActive(true)
                    .build(),
                SubscriptionPlan.builder()
                    .name("PRO")
                    .description("For medium-sized schools. Up to 1000 students.")
                    .priceMonthly(new BigDecimal("2499.00"))
                    .priceAnnually(new BigDecimal("24999.00"))
                    .maxStudents(1000)
                    .maxTeachers(100)
                    .maxClasses(50)
                    .storageGb(25)
                    .isActive(true)
                    .build(),
                SubscriptionPlan.builder()
                    .name("ENTERPRISE")
                    .description("Unlimited students. Custom pricing available.")
                    .priceMonthly(new BigDecimal("5999.00"))
                    .priceAnnually(new BigDecimal("59999.00"))
                    .maxStudents(null) // Unlimited
                    .maxTeachers(null)
                    .maxClasses(null)
                    .storageGb(100)
                    .isActive(true)
                    .build()
            );
            planRepository.saveAll(plans);
            log.info("Seeded {} subscription plans.", plans.size());
        }
    }

    private void seedProjectAdmin() {
        if (!userRepository.existsByEmail("admin@pathshalapro.com")) {
            Role adminRole = roleRepository.findByName(RoleName.PROJECT_ADMIN)
                    .orElseThrow(() -> new RuntimeException("PROJECT_ADMIN role not found. Seed roles first."));

            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("admin@pathshalapro.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .isActive(true)
                    .isEmailVerified(true)
                    .roles(List.of(adminRole))
                    .build();

            userRepository.save(admin);
            log.info("Seeded PROJECT_ADMIN: admin@pathshalapro.com / Admin@123");
        }
    }

    private void seedDemoSchool() {
        if (!schoolRepository.existsByCode("DEMO001")) {
            School school = School.builder()
                    .name("Delhi Public School - Demo")
                    .code("DEMO001")
                    .address("123 Demo Street, Model Town")
                    .city("New Delhi")
                    .state("Delhi")
                    .pincode("110009")
                    .phone("+91-9876543210")
                    .email("demo@dpsdemo.edu.in")
                    .isActive(true)
                    .subscriptionStatus(SubscriptionStatus.TRIAL)
                    .build();

            schoolRepository.save(school);
            log.info("Seeded demo school: DEMO001");
        }
    }

    private String getRoleDescription(RoleName roleName) {
        return switch (roleName) {
            case PROJECT_ADMIN -> "Super administrator with access to all schools and system settings.";
            case SCHOOL_ADMIN -> "Administrator of a specific school with full school-level access.";
            case TEACHER -> "Teacher with access to class management, attendance, and marks.";
            case STUDENT -> "Student with access to their own data, notes, and results.";
            case PARENT -> "Parent with read-only access to their child's data.";
        };
    }
}
