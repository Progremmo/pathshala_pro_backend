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
    private final TimetableRepository timetableRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.pathshalapro.service.SchoolConfigService schoolConfigService;
    private final SystemSettingRepository systemSettingRepository;

    private final LateFeeRuleRepository lateFeeRuleRepository;
    private final FeeInstallmentPlanRepository feeInstallmentPlanRepository;
    private final AdvanceCreditRepository advanceCreditRepository;
    private final StudentFeeConcessionRepository studentFeeConcessionRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final MarksRepository marksRepository;
    private final NotesRepository notesRepository;
    private final AnnouncementRepository announcementRepository;
    private final OnlineClassRepository onlineClassRepository;

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
            seedEnhancedFeeData();
            seedSystemSettings();
            seedTimetable2026();
            seedOtherModulesData();
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

    private void seedSystemSettings() {
        if (systemSettingRepository.count() == 0) {
            log.info("Seeding platform-wide system settings...");
            List<SystemSetting> settings = List.of(
                    // General
                    SystemSetting.builder().configKey("PLATFORM_NAME").configValue("PathshalaPro").configGroup("GENERAL")
                            .description("Name of the platform").build(),
                    SystemSetting.builder().configKey("SUPPORT_EMAIL").configValue("support@pathshalapro.com")
                            .configGroup("GENERAL").description("Primary support email address").build(),
                    SystemSetting.builder().configKey("CONTACT_PHONE").configValue("+91 9795384656")
                            .configGroup("GENERAL").description("Primary contact phone number").build(),
                    SystemSetting.builder().configKey("BASE_CURRENCY").configValue("INR").configGroup("GENERAL")
                            .description("Base currency for the platform").build(),

                    // Email (SMTP)
                    SystemSetting.builder().configKey("SMTP_HOST").configValue("smtp.gmail.com").configGroup("EMAIL")
                            .description("SMTP server host").build(),
                    SystemSetting.builder().configKey("SMTP_PORT").configValue("465").configGroup("EMAIL")
                            .description("SMTP server port").build(),
                    SystemSetting.builder().configKey("SMTP_USERNAME").configValue("noreply@pathshalapro.com")
                            .configGroup("EMAIL").description("SMTP username").build(),

                    // Security
                    SystemSetting.builder().configKey("ENABLE_MFA").configValue("false").configGroup("SECURITY")
                            .description("Enable Multi-Factor Authentication platform-wide").build(),
                    SystemSetting.builder().configKey("MAX_LOGIN_ATTEMPTS").configValue("5").configGroup("SECURITY")
                            .description("Maximum failed login attempts before lockout").build()
            );
            systemSettingRepository.saveAll(settings);
            log.info("Seeded {} system settings.", settings.size());
        }
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
            ClassRoom class10A = seedClassRoom(school, "Class 10", "A", "10", "2026-27");
            ClassRoom class10B = seedClassRoom(school, "Class 10", "B", "10", "2026-27");
            seedClassRoom(school, "Class 9", "A", "9", "2026-27");

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
            userRepository.findByEmailAndIsDeletedFalse("student@demo.com").ifPresent(mainStudent -> {
                assignStudentToClass(mainStudent, class10A);
                userRepository.findByEmailAndIsDeletedFalse("parent@demo.com").ifPresent(parent -> {
                    mainStudent.setParent(parent);
                    userRepository.save(mainStudent);
                });
            });
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
        student.setClassRoom(classRoom);
        userRepository.save(student);
        log.info("Assigned student {} to class {} ({})", student.getEmail(), classRoom.getName(), classRoom.getAcademicYear());
    }

    private void seedAttendance(School school, User student, ClassRoom classRoom, int totalDays, int presentDays) {
        attendanceRepository.deleteAll(attendanceRepository.findByStudentIdAndIsDeletedFalse(student.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent());
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
                                school.getId(), "2026-27")
                        .ifPresent(cls -> {
                            feeAllocationRepository.save(FeeAllocation.builder()
                                    .school(school).classRoom(cls).feeGroup(stdGroup).academicYear("2026-27").build());
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

    // ======================== TIMETABLE SEEDER 2026-27 ========================

    private void seedTimetable2026() {
        schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").ifPresent(school -> {
            // Skip if timetable data already exists for 2026-27
            List<Timetable> existing = timetableRepository
                    .findBySchoolIdAndDayOfWeekAndIsDeletedFalse(school.getId(), DayOfWeek.MONDAY);
            boolean has2026 = existing.stream().anyMatch(t -> "2026-27".equals(t.getAcademicYear()));
            if (has2026) {
                log.info("Timetable for 2026-27 already seeded. Skipping.");
                return;
            }

            log.info("Seeding timetable data for 2026-27...");

            // 1. Classrooms for 2026-27
            ClassRoom c10A = seedClassRoom(school, "Class 10", "A", "10", "2026-27");
            ClassRoom c10B = seedClassRoom(school, "Class 10", "B", "10", "2026-27");
            ClassRoom c9A  = seedClassRoom(school, "Class 9",  "A", "9",  "2026-27");

            // 2. Subjects (grade 10 + grade 9) — idempotent via code lookup
            Subject math   = seedSubject(school, "Mathematics",      "MATH101",  "10");
            Subject sci    = seedSubject(school, "Science",           "SCI101",   "10");
            Subject eng    = seedSubject(school, "English",           "ENG101",   "10");
            Subject hindi  = seedSubject(school, "Hindi",             "HIN101",   "10");
            Subject sst    = seedSubject(school, "Social Studies",    "SST101",   "10");
            Subject cs     = seedSubject(school, "Computer Science",  "CS101",    "10");
            Subject math9  = seedSubject(school, "Mathematics",       "MATH901",  "9");
            Subject sci9   = seedSubject(school, "Science",           "SCI901",   "9");
            Subject eng9   = seedSubject(school, "English",           "ENG901",   "9");
            Subject hist9  = seedSubject(school, "History",           "HIST901",  "9");

            // 3. Teachers — reuse existing + seed more
            User teacher1 = seedUser(school, "John",    "Doe",     "teacher@demo.com",     RoleName.TEACHER); // Math & CS
            User teacher2 = seedUser(school, "Priya",   "Sharma",  "priya@demo.com",       RoleName.TEACHER); // Science
            User teacher3 = seedUser(school, "Rahul",   "Verma",   "rahul@demo.com",       RoleName.TEACHER); // English & Hindi
            User teacher4 = seedUser(school, "Sneha",   "Gupta",   "sneha@demo.com",       RoleName.TEACHER); // SST & History

            // 4. Period timings (8 periods, 40 min each, with breaks)
            LocalTime[][] periods = {
                { LocalTime.of(8, 0),  LocalTime.of(8, 40)  },  // Period 1
                { LocalTime.of(8, 40), LocalTime.of(9, 20)  },  // Period 2
                { LocalTime.of(9, 20), LocalTime.of(10, 0)  },  // Period 3
                { LocalTime.of(10, 15),LocalTime.of(10, 55) },  // Period 4 (after break)
                { LocalTime.of(10, 55),LocalTime.of(11, 35) },  // Period 5
                { LocalTime.of(11, 35),LocalTime.of(12, 15) },  // Period 6
                { LocalTime.of(13, 0), LocalTime.of(13, 40) },  // Period 7 (after lunch)
                { LocalTime.of(13, 40),LocalTime.of(14, 20) },  // Period 8
            };

            DayOfWeek[] weekDays = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
            };

            // 5. Class 10-A timetable (subject rotation per day)
            Subject[][] schedule10A = {
                // Mon:     Math,  Eng,  Sci,  Hindi, CS,   SST,   Math,  Sci
                { math, eng, sci, hindi, cs, sst, math, sci },
                // Tue:     Eng,   Math, Hindi, Sci,  SST,  CS,    Eng,   Math
                { eng, math, hindi, sci, sst, cs, eng, math },
                // Wed:     Sci,   Hindi, Math, CS,  Eng,   Math,  SST,   Hindi
                { sci, hindi, math, cs, eng, math, sst, hindi },
                // Thu:     Hindi, Sci,  Eng,  Math, Math,  CS,    Sci,   SST
                { hindi, sci, eng, math, math, cs, sci, sst },
                // Fri:     CS,    Math, SST,  Eng,  Sci,   Hindi, Math,  Eng
                { cs, math, sst, eng, sci, hindi, math, eng },
                // Sat:     Math,  SST,  Sci,  Eng,  Hindi, Math,  CS,    Sci
                { math, sst, sci, eng, hindi, math, cs, sci },
            };

            // 6. Class 10-B timetable (different rotation)
            Subject[][] schedule10B = {
                { sci, math, eng, cs, hindi, math, sst, eng },
                { math, sci, cs, eng, math, hindi, eng, sst },
                { eng, cs, sci, math, sst, hindi, math, sci },
                { cs, eng, math, hindi, sci, sst, hindi, math },
                { hindi, math, eng, sst, cs, sci, eng, math },
                { sst, hindi, math, sci, eng, cs, sci, math },
            };

            // 7. Class 9-A timetable
            Subject[][] schedule9A = {
                { math9, eng9, sci9, hist9, math9, sci9, eng9, hist9 },
                { sci9, math9, hist9, eng9, sci9, math9, hist9, eng9 },
                { eng9, hist9, math9, sci9, eng9, hist9, math9, sci9 },
                { hist9, sci9, eng9, math9, hist9, eng9, sci9, math9 },
                { math9, eng9, hist9, sci9, math9, eng9, sci9, hist9 },
                { sci9, hist9, eng9, math9, sci9, math9, eng9, hist9 },
            };

            // Teacher assignment map for grade 10
            // math->teacher1, sci->teacher2, eng->teacher3, hindi->teacher3, sst->teacher4, cs->teacher1
            java.util.Map<Long, User> teacherMap10 = java.util.Map.of(
                math.getId(), teacher1,
                sci.getId(),  teacher2,
                eng.getId(),  teacher3,
                hindi.getId(),teacher3,
                sst.getId(),  teacher4,
                cs.getId(),   teacher1
            );

            // Teacher assignment for grade 9
            // math9->teacher1, sci9->teacher2, eng9->teacher3, hist9->teacher4
            java.util.Map<Long, User> teacherMap9 = java.util.Map.of(
                math9.getId(), teacher1,
                sci9.getId(),  teacher2,
                eng9.getId(),  teacher3,
                hist9.getId(), teacher4
            );

            // Seed the three timetables
            seedClassTimetable(school, c10A, schedule10A, weekDays, periods, teacherMap10, "2026-27");
            seedClassTimetable(school, c10B, schedule10B, weekDays, periods, teacherMap10, "2026-27");
            seedClassTimetable(school, c9A,  schedule9A,  weekDays, periods, teacherMap9,  "2026-27");

            log.info("Seeded full timetable for 2026-27: 3 classrooms × 6 days × 8 periods = {} entries",
                    3 * 6 * 8);
        });
    }

    private void seedClassTimetable(School school, ClassRoom classRoom, Subject[][] schedule,
                                     DayOfWeek[] weekDays, LocalTime[][] periods,
                                     java.util.Map<Long, User> teacherMap, String academicYear) {
        for (int d = 0; d < weekDays.length; d++) {
            for (int p = 0; p < periods.length; p++) {
                Subject subject = schedule[d][p];
                User teacher = teacherMap.get(subject.getId());
                if (teacher == null) continue;

                Timetable tt = Timetable.builder()
                        .school(school)
                        .classRoom(classRoom)
                        .subject(subject)
                        .teacher(teacher)
                        .dayOfWeek(weekDays[d])
                        .startTime(periods[p][0])
                        .endTime(periods[p][1])
                        .periodNumber(p + 1)
                        .academicYear(academicYear)
                        .build();
                timetableRepository.save(tt);
            }
        }
    }
    private void seedEnhancedFeeData() {
        schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").ifPresent(school -> {
            log.info("Seeding enhanced fee data (MVP+) for demo school...");

            // 1. Late Fee Rules
            if (lateFeeRuleRepository.findBySchoolId(school.getId()).isEmpty()) {
                lateFeeRuleRepository.save(LateFeeRule.builder()
                        .school(school).ruleType("FIXED").ruleValue(new BigDecimal("100.00")).gracePeriodDays(5).build());
                lateFeeRuleRepository.save(LateFeeRule.builder()
                        .school(school).ruleType("PERCENTAGE").ruleValue(new BigDecimal("2.00")).gracePeriodDays(15).build());
                log.info("Seeded Late Fee Rules.");
            }

            // 2. Installment Plans
            if (feeInstallmentPlanRepository.findBySchoolIdAndAcademicYear(school.getId(), "2026-27").isEmpty()) {
                FeeInstallmentPlan plan = FeeInstallmentPlan.builder()
                        .school(school).name("Annual Standard Split").totalAmount(new BigDecimal("60000.00"))
                        .academicYear("2026-27").build();
                
                plan.setInstallments(List.of(
                        FeeInstallment.builder().feeInstallmentPlan(plan).installmentNumber(1).amount(new BigDecimal("15000.00")).dueDate(LocalDate.of(2024, 4, 15)).build(),
                        FeeInstallment.builder().feeInstallmentPlan(plan).installmentNumber(2).amount(new BigDecimal("15000.00")).dueDate(LocalDate.of(2024, 7, 15)).build(),
                        FeeInstallment.builder().feeInstallmentPlan(plan).installmentNumber(3).amount(new BigDecimal("15000.00")).dueDate(LocalDate.of(2024, 10, 15)).build(),
                        FeeInstallment.builder().feeInstallmentPlan(plan).installmentNumber(4).amount(new BigDecimal("15000.00")).dueDate(LocalDate.of(2025, 1, 15)).build()
                ));
                feeInstallmentPlanRepository.save(plan);
                log.info("Seeded Fee Installment Plans.");
            }

            // 3. Concessions & Advance Credit
            userRepository.findByEmailAndIsDeletedFalse("student@demo.com").ifPresent(student -> {
                if (studentFeeConcessionRepository.findByStudentId(student.getId()).isEmpty()) {
                    studentFeeConcessionRepository.save(StudentFeeConcession.builder()
                            .student(student).discountType("PERCENTAGE").value(new BigDecimal("50.00"))
                            .reason("Staff Child Concession").build());
                    log.info("Seeded Student Fee Concession for student@demo.com.");
                }

                if (advanceCreditRepository.findByStudentId(student.getId()).isEmpty()) {
                    advanceCreditRepository.save(AdvanceCredit.builder()
                            .student(student).creditAmount(new BigDecimal("10000.00")).build());
                    log.info("Seeded Advance Credit for student@demo.com.");
                }

                // 4. Invoices (Pending, Partial, Paid)
                if (feeInvoiceRepository.findByStudentIdAndIsDeletedFalse(student.getId(), org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                    // Create an overdue pending invoice
                    FeeInvoice overdueInvoice = FeeInvoice.builder()
                            .school(school).student(student).invoiceNumber("INV-DEMO-001")
                            .totalAmount(new BigDecimal("5000.00")).netAmount(new BigDecimal("5000.00"))
                            .paidAmount(BigDecimal.ZERO).paymentStatus(PaymentStatus.PENDING)
                            .dueDate(LocalDate.now().minusDays(10)).periodMonth(4).periodYear(2026).academicYear("2026-27").remarks("Overdue Tuition").build();
                    feeInvoiceRepository.save(overdueInvoice);

                    // Create a paid invoice
                    FeeInvoice paidInvoice = FeeInvoice.builder()
                            .school(school).student(student).invoiceNumber("INV-DEMO-002")
                            .totalAmount(new BigDecimal("5000.00")).netAmount(new BigDecimal("5000.00"))
                            .paidAmount(new BigDecimal("5000.00")).paymentStatus(PaymentStatus.PAID)
                            .dueDate(LocalDate.now().minusDays(40)).periodMonth(3).periodYear(2026).academicYear("2026-27").remarks("Paid Tuition").build();
                    paidInvoice = feeInvoiceRepository.save(paidInvoice);

                    // Add a payment for the paid invoice
                    paymentRepository.save(Payment.builder()
                            .school(school).feeInvoice(paidInvoice).paidBy(student)
                            .amount(new BigDecimal("5000.00")).currency("INR")
                            .status(PaymentStatus.PAID).paymentMethod("ONLINE")
                            .razorpayOrderId("order_demo_123").razorpayPaymentId("pay_demo_123").razorpaySignature("demo_sig")
                            .receiptNumber("REC-DEMO-002").paymentDate(java.time.LocalDateTime.now().minusDays(38))
                            .build());

                    log.info("Seeded Sample Invoices and Payments for student@demo.com.");
                }
            });

            // Seed fees for Alice
            userRepository.findByEmailAndIsDeletedFalse("alice@demo.com").ifPresent(alice -> {
                if (feeInvoiceRepository.findByStudentIdAndIsDeletedFalse(alice.getId(), org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                    FeeInvoice pendingInvoice = FeeInvoice.builder()
                            .school(school).student(alice).invoiceNumber("INV-DEMO-ALICE")
                            .totalAmount(new BigDecimal("4500.00")).netAmount(new BigDecimal("4500.00"))
                            .paidAmount(BigDecimal.ZERO).paymentStatus(PaymentStatus.PENDING)
                            .dueDate(LocalDate.now().plusDays(5)).periodMonth(4).periodYear(2026).academicYear("2026-27").remarks("Upcoming Tuition").build();
                    feeInvoiceRepository.save(pendingInvoice);
                }
            });
        });
    }

    private void seedOtherModulesData() {
        schoolRepository.findByCodeAndIsDeletedFalse("DEMO001").ifPresent(school -> {
            log.info("Seeding other modules data (Marks, Notes, Announcements, OnlineClasses) for demo school...");

            // 1. Announcements
            if (announcementRepository.findBySchoolIdAndIsDeletedFalse(school.getId(), org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                userRepository.findByEmailAndIsDeletedFalse("admin@demo.com").ifPresent(admin -> {
                    announcementRepository.save(Announcement.builder()
                            .school(school)
                            .createdByUser(admin)
                            .title("Welcome to the new Academic Year 2026-27")
                            .content("Classes for the new academic year will commence next Monday. Please ensure you have all required textbooks.")
                            .targetAudience("ALL")
                            .build());
                    announcementRepository.save(Announcement.builder()
                            .school(school)
                            .createdByUser(admin)
                            .title("PTA Meeting Schedule")
                            .content("The Parent-Teacher Association meeting for Class 10 is scheduled for this Friday at 10:00 AM in the main auditorium.")
                            .targetAudience("PARENT")
                            .build());
                    log.info("Seeded Announcements.");
                });
            }

            // 2. Online Classes
            if (onlineClassRepository.findBySchoolIdAndIsDeletedFalse(school.getId(), org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                userRepository.findByEmailAndIsDeletedFalse("teacher@demo.com").ifPresent(teacher -> {
                    classRoomRepository.findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse("Class 10", "A", school.getId(), "2026-27").ifPresent(cls -> {
                        subjectRepository.findByCodeAndSchoolIdAndIsDeletedFalse("MATH101", school.getId()).ifPresent(subject -> {
                            onlineClassRepository.save(OnlineClass.builder()
                                    .school(school)
                                    .classRoom(cls)
                                    .subject(subject)
                                    .teacher(teacher)
                                    .title("Introduction to Trigonometry")
                                    .meetingLink("https://meet.google.com/abc-defg-hij")
                                    .platform("Google Meet")
                                    .scheduledAt(java.time.LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                                    .durationMinutes(45)
                                    .status("SCHEDULED")
                                    .build());
                            log.info("Seeded Online Classes.");
                        });
                    });
                });
            }

            // 3. Notes
            if (notesRepository.findBySchoolIdAndIsDeletedFalse(school.getId(), org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                userRepository.findByEmailAndIsDeletedFalse("teacher@demo.com").ifPresent(teacher -> {
                    classRoomRepository.findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse("Class 10", "A", school.getId(), "2026-27").ifPresent(cls -> {
                        subjectRepository.findByCodeAndSchoolIdAndIsDeletedFalse("SCI101", school.getId()).ifPresent(subject -> {
                            notesRepository.save(Notes.builder()
                                    .school(school)
                                    .grade(cls.getName())
                                    .academicYear(cls.getAcademicYear())
                                    .subject(subject)
                                    .uploadedBy(teacher)
                                    .title("Chapter 1: Chemical Reactions")
                                    .description("Detailed notes covering balancing chemical equations.")
                                    .contentUrl("https://example.com/notes/chem-chapter1.pdf")
                                    .contentType("pdf")
                                    .build());
                            log.info("Seeded Notes.");
                        });
                    });
                });
            }

            // 4. Marks (Exam Results)
            if (marksRepository.count() == 0) {
                userRepository.findByEmailAndIsDeletedFalse("student@demo.com").ifPresent(student -> {
                    examRepository.findBySchoolIdAndIsDeletedFalse(school.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().stream().filter(e -> "Mid-Term Math Exam".equals(e.getName())).findFirst().ifPresent(exam -> {
                        marksRepository.save(Marks.builder()
                                .school(school)
                                .exam(exam)
                                .student(student)
                                .marksObtained(85.5)
                                .remarks("Excellent performance!")
                                .build());
                        log.info("Seeded Marks for student@demo.com.");
                    });
                });
                
                userRepository.findByEmailAndIsDeletedFalse("alice@demo.com").ifPresent(student -> {
                    examRepository.findBySchoolIdAndIsDeletedFalse(school.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().stream().filter(e -> "Mid-Term Math Exam".equals(e.getName())).findFirst().ifPresent(exam -> {
                        marksRepository.save(Marks.builder()
                                .school(school)
                                .exam(exam)
                                .student(student)
                                .marksObtained(92.0)
                                .remarks("Outstanding!")
                                .build());
                        log.info("Seeded Marks for alice@demo.com.");
                    });
                });
            }
        });
    }
}
