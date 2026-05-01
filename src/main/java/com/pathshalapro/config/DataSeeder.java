package com.pathshalapro.config;

import com.pathshalapro.entity.*;
import com.pathshalapro.entity.enums.*;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data seeder - runs on application startup.
 * Seeds roles, subscription plans, and a default PROJECT_ADMIN user.
 * Only runs in 'default' and 'dev' profiles (not in production tests).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SubscriptionPlanRepository planRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamRepository examRepository;
    private final FeeHeadRepository feeHeadRepository;
    private final FeeGroupRepository feeGroupRepository;
    private final FeeAllocationRepository feeAllocationRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.pathshalapro.service.SchoolConfigService schoolConfigService;

    @Bean
    @Profile({ "default", "dev", "prod" })
    public CommandLineRunner seedData() {
        return args -> {
            log.info("==== PathshalaPro Data Seeder Starting ====");
            seedRoles();
            seedSubscriptionPlans();
            seedProjectAdmin();
            seedDemoSchool();
            seedSchoolConfigs();
            seedDemoDataForSchool();
            seedFeeData();
            log.info("==== Data Seeder Completed ====");
        };
    }

    private void seedSchoolConfigs() {
        schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").ifPresent(school -> {
            log.info("Seeding configurations for demo school...");
            schoolConfigService.saveConfig(school.getId(), "ACADEMIC_YEARS", "[\"2024-25\", \"2025-26\", \"2026-27\"]");
            schoolConfigService.saveConfig(school.getId(), "FEE_TYPES",
                    "[\"TUITION\", \"TRANSPORT\", \"LIBRARY\", \"LABORATORY\", \"SPORTS\", \"EXAM\", \"ADMISSION\", \"OTHER\"]");
            schoolConfigService.saveConfig(school.getId(), "FEE_FREQUENCIES",
                    "[\"MONTHLY\", \"QUARTERLY\", \"HALF_YEARLY\", \"ANNUALLY\", \"ONE_TIME\"]");
            schoolConfigService.saveConfig(school.getId(), "EXAM_TYPES",
                    "{\"UNIT_TEST\":\"Unit Test\",\"MID_TERM\":\"Mid Term\",\"FINAL_TERM\":\"Final Term\",\"INTERNAL\":\"Internal\",\"PRACTICAL\":\"Practical\",\"QUIZ\":\"Quiz\",\"ASSIGNMENT\":\"Assignment\"}");
        });
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
                            .build());
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
        School school;
        if (!schoolRepository.existsByCode("DEMO001")) {
            school = School.builder()
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

            school = schoolRepository.save(school);
            log.info("Seeded demo school: DEMO001");
        } else {
            school = schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").orElse(null);
        }

        if (school != null) {
            // Seed School Admin
            seedUser(school, "School", "Admin", "school@demo.com", RoleName.SCHOOL_ADMIN);
            // Seed Teacher
            seedUser(school, "John", "Doe", "teacher@demo.com", RoleName.TEACHER);
            // Seed Student
            seedUser(school, "Jane", "Doe", "student@demo.com", RoleName.STUDENT);
            // Seed Parent
            seedUser(school, "Parent", "Account", "parent@demo.com", RoleName.PARENT);
        }
    }

    private void seedDemoDataForSchool() {
        schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").ifPresent(school -> {
            log.info("Seeding comprehensive demo data for school: {}", school.getName());

            // 1. Seed Classrooms
            ClassRoom class10A = seedClassRoom(school, "Class 10", "A", "10", "2024-25");
            ClassRoom class10B = seedClassRoom(school, "Class 10", "B", "10", "2024-25");
            seedClassRoom(school, "Class 9", "A", "9", "2024-25");

            // 2. Seed Subjects
            Subject math = seedSubject(school, "Mathematics", "MATH101", "10");
            Subject science = seedSubject(school, "Science", "SCI101", "10");
            seedSubject(school, "English", "ENG101", "10");
            seedSubject(school, "History", "HIST901", "9");

            // 3. Seed More Students
            User student1 = seedUser(school, "Alice", "Johnson", "alice@demo.com", RoleName.STUDENT);
            User student2 = seedUser(school, "Bob", "Smith", "bob@demo.com", RoleName.STUDENT);
            User student3 = seedUser(school, "Charlie", "Brown", "charlie@demo.com", RoleName.STUDENT);

            // Assign students to classrooms
            assignStudentToClass(student1, class10A);
            assignStudentToClass(student2, class10A);
            assignStudentToClass(student3, class10B);

            // 4. Seed Attendance for Alice
            seedAttendance(school, student1, class10A, 20, 18); // 20 days total, 18 present

            // 5. Seed Upcoming Exams
            seedExam(school, class10A, math, "Mid-Term Math Exam", ExamType.MID_TERM, LocalDate.now().plusDays(5));
            seedExam(school, class10A, science, "Unit Test - Physics", ExamType.UNIT_TEST, LocalDate.now().plusDays(2));
        });
    }

    private ClassRoom seedClassRoom(School school, String name, String section, String grade, String academicYear) {
        return classRoomRepository
                .findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(name, section, school.getId(),
                        academicYear)
                .orElseGet(() -> {
                    ClassRoom cr = ClassRoom.builder()
                            .school(school)
                            .name(name)
                            .section(section)
                            .grade(grade)
                            .academicYear(academicYear)
                            .capacity(40)
                            .build();
                    ClassRoom saved = classRoomRepository.save(cr);
                    log.info("Seeded classroom: {} - {}", name, section);
                    return saved;
                });
    }

    private Subject seedSubject(School school, String name, String code, String grade) {
        return subjectRepository.findByCodeAndSchoolIdAndIsDeletedFalse(code, school.getId())
                .orElseGet(() -> {
                    Subject s = Subject.builder()
                            .school(school)
                            .name(name)
                            .code(code)
                            .grade(grade)
                            .build();
                    Subject saved = subjectRepository.save(s);
                    log.info("Seeded subject: {}", name);
                    return saved;
                });
    }

    private void assignStudentToClass(User student, ClassRoom classRoom) {
        if (student.getClassRoom() == null) {
            student.setClassRoom(classRoom);
            userRepository.save(student);
            log.info("Assigned student {} to class {}", student.getEmail(), classRoom.getName());
        }
    }

    private void seedAttendance(School school, User student, ClassRoom classRoom, int totalDays, int presentDays) {
        if (attendanceRepository.countByStudentIdAndIsDeletedFalse(student.getId()) == 0) {
            LocalDate date = LocalDate.now().minusDays(totalDays);
            for (int i = 0; i < totalDays; i++) {
                AttendanceStatus status = (i < presentDays) ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
                Attendance attendance = Attendance.builder()
                        .school(school)
                        .student(student)
                        .classRoom(classRoom)
                        .attendanceDate(date)
                        .status(status)
                        .build();
                attendanceRepository.save(attendance);
                date = date.plusDays(1);
            }
            log.info("Seeded attendance for student: {}", student.getEmail());
        }
    }

    private void seedExam(School school, ClassRoom classRoom, Subject subject, String title, ExamType type,
            LocalDate date) {
        if (!examRepository.existsBySchoolIdAndNameAndIsDeletedFalse(school.getId(), title)) {
            Exam exam = Exam.builder()
                    .school(school)
                    .classRoom(classRoom)
                    .subject(subject)
                    .name(title)
                    .examType(type)
                    .examDate(date)
                    .startTime(LocalTime.of(10, 0))
                    .durationMinutes(180)
                    .totalMarks(100.0)
                    .passingMarks(33.0)
                    .academicYear(classRoom.getAcademicYear())
                    .build();
            examRepository.save(exam);
            log.info("Seeded exam: {}", title);
        }
    }

    private User seedUser(School school, String first, String last, String email, RoleName roleName) {
        Optional<User> existing = userRepository.findByEmailAndIsDeletedFalse(email);
        if (existing.isEmpty()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException(roleName + " role not found."));

            User user = User.builder()
                    .firstName(first)
                    .lastName(last)
                    .email(email)
                    .password(passwordEncoder.encode("Demo@123"))
                    .isActive(true)
                    .isEmailVerified(true)
                    .school(school)
                    .roles(List.of(role))
                    .build();

            User saved = userRepository.save(user);
            log.info("Seeded {}: {} / Demo@123", roleName, email);
            return saved;
        }
        return existing.get();
    }

    private void seedFeeData() {
        schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").ifPresent(school -> {
            if (feeHeadRepository.countBySchoolIdAndIsDeletedFalse(school.getId()) == 0) {
                log.info("Seeding fee data for demo school...");

                // 1. Fee Heads
                FeeHead tuition = feeHeadRepository.save(FeeHead.builder()
                        .name("Tuition Fee").description("Standard tuition fee").isMandatory(true).school(school)
                        .build());
                FeeHead transport = feeHeadRepository.save(FeeHead.builder()
                        .name("Transport Fee").description("School bus fee").isMandatory(false).school(school).build());
                FeeHead library = feeHeadRepository.save(FeeHead.builder()
                        .name("Library Fee").description("Library access fee").isMandatory(true).school(school)
                        .build());

                // 2. Fee Group
                FeeGroup stdGroup = feeGroupRepository.save(FeeGroup.builder()
                        .name("Class 10 Standard Monthly").description("Monthly tuition and library fees")
                        .school(school).build());

                stdGroup.setFeeItems(List.of(
                        FeeGroupItem.builder().feeGroup(stdGroup).feeHead(tuition).amount(new BigDecimal("5000.00"))
                                .build(),
                        FeeGroupItem.builder().feeGroup(stdGroup).feeHead(library).amount(new BigDecimal("500.00"))
                                .build(),
                        FeeGroupItem.builder().feeGroup(stdGroup).feeHead(transport).amount(new BigDecimal("2500.00"))
                                .build()));
                feeGroupRepository.save(stdGroup);

                // 3. Allocations
                classRoomRepository
                        .findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse("Class 10", "A",
                                school.getId(), "2024-25")
                        .ifPresent(cls -> {
                            feeAllocationRepository.save(FeeAllocation.builder()
                                    .school(school).classRoom(cls).feeGroup(stdGroup).academicYear("2024-25").build());
                            log.info("Allocated fees to Class 10-A");
                        });
            }
        });
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
