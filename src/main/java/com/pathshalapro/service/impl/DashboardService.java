package com.pathshalapro.service.impl;

import com.pathshalapro.dto.dashboard.DashboardStatsResponse;
import com.pathshalapro.dto.dashboard.TeacherDashboardResponse;
import com.pathshalapro.entity.Timetable;
import com.pathshalapro.entity.enums.AttendanceStatus;
import com.pathshalapro.entity.enums.DayOfWeek;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.repository.AttendanceRepository;
import com.pathshalapro.repository.FeeInvoiceRepository;
import com.pathshalapro.repository.NotesRepository;
import com.pathshalapro.repository.OnlineClassRepository;
import com.pathshalapro.repository.TimetableRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final UserRepository userRepository;
        private final AttendanceRepository attendanceRepository;
        private final FeeInvoiceRepository feeInvoiceRepository;
        private final TimetableRepository timetableRepository;
        private final NotesRepository notesRepository;
        private final OnlineClassRepository onlineClassRepository;

        @Transactional(readOnly = true)
        public DashboardStatsResponse getSchoolStats(Long schoolId) {
                LocalDate today = LocalDate.now();
                LocalDate startOfMonth = today.withDayOfMonth(1);

                // Basic Counts
                long totalStudents = userRepository.countBySchoolIdAndRoleName(schoolId, RoleName.STUDENT);
                long totalTeachers = userRepository.countBySchoolIdAndRoleName(schoolId, RoleName.TEACHER);

                // Fees
                BigDecimal monthlyCollection = feeInvoiceRepository.getCollectionSince(schoolId,
                                startOfMonth.atStartOfDay());
                BigDecimal monthlyPending = feeInvoiceRepository.getOutstandingSince(schoolId, startOfMonth);

                // Attendance Today
                long totalAttendanceMarked = attendanceRepository.countBySchoolIdAndDate(schoolId, today);
                long presentToday = attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today,
                                AttendanceStatus.PRESENT);
                long lateToday = attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today,
                                AttendanceStatus.LATE);

                double attendancePct = totalAttendanceMarked > 0
                                ? (double) (presentToday + lateToday) / totalAttendanceMarked * 100
                                : 0;

                // Fee Trend (Last 4 months)
                List<DashboardStatsResponse.FeeTrendData> feeTrend = new ArrayList<>();
                for (int i = 3; i >= 0; i--) {
                        LocalDate monthDate = today.minusMonths(i);
                        LocalDate start = monthDate.withDayOfMonth(1);

                        BigDecimal coll = feeInvoiceRepository.getCollectionSince(schoolId, start.atStartOfDay());
                        BigDecimal pend = feeInvoiceRepository.getOutstandingSince(schoolId, start);

                        feeTrend.add(DashboardStatsResponse.FeeTrendData.builder()
                                        .month(monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                                        .collected(coll != null ? coll : BigDecimal.ZERO)
                                        .pending(pend != null ? pend : BigDecimal.ZERO)
                                        .build());
                }

                // Attendance Distribution Today
                List<DashboardStatsResponse.AttendanceDistribution> distribution = new ArrayList<>();
                distribution.add(new DashboardStatsResponse.AttendanceDistribution("Present", presentToday, "#10b981"));
                distribution.add(new DashboardStatsResponse.AttendanceDistribution("Absent",
                                attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today,
                                                AttendanceStatus.ABSENT),
                                "#ef4444"));
                distribution.add(new DashboardStatsResponse.AttendanceDistribution("Late", lateToday, "#f59e0b"));
                distribution.add(new DashboardStatsResponse.AttendanceDistribution("Leave",
                                attendanceRepository.countBySchoolIdAndDateAndStatus(schoolId, today,
                                                AttendanceStatus.LEAVE),
                                "#8b5cf6"));

                return DashboardStatsResponse.builder()
                                .totalStudents(totalStudents)
                                .totalTeachers(totalTeachers)
                                .monthlyCollection(monthlyCollection != null ? monthlyCollection : BigDecimal.ZERO)
                                .monthlyPending(monthlyPending != null ? monthlyPending : BigDecimal.ZERO)
                                .todayAttendancePercentage(Math.round(attendancePct * 10.0) / 10.0)
                                .feeTrend(feeTrend)
                                .attendanceDistribution(distribution)
                                .build();
        }

        @Transactional(readOnly = true)
        public TeacherDashboardResponse getTeacherStats(Long schoolId, Long teacherId) {
                LocalDate today = LocalDate.now();
                LocalDateTime now = LocalDateTime.now();
                DayOfWeek currentDay = DayOfWeek.valueOf(today.getDayOfWeek().name());

                String academicYear = "2024-25";

                // 1. Classes Today
                List<Timetable> todayTimetable = timetableRepository
                                .findByTeacherIdAndDayOfWeekAndAcademicYearAndIsDeletedFalse(teacherId, currentDay,
                                                academicYear);
                long classesToday = todayTimetable.size();

                // 2. Pending Attendance
                // Get all classrooms the teacher has classes in today
                Set<Long> classroomIds = todayTimetable.stream()
                                .map(t -> t.getClassRoom().getId())
                                .collect(Collectors.toSet());

                long pendingAttendanceCount = 0;
                List<TeacherDashboardResponse.TeacherTaskDto> pendingTasks = new ArrayList<>();

                for (Long classRoomId : classroomIds) {
                        boolean marked = attendanceRepository
                                        .existsByClassRoomIdAndAttendanceDateAndIsDeletedFalse(classRoomId, today);
                        if (!marked) {
                                pendingAttendanceCount++;
                                // Find classroom name for task
                                todayTimetable.stream()
                                                .filter(t -> t.getClassRoom().getId().equals(classRoomId))
                                                .findFirst()
                                                .ifPresent(t -> pendingTasks.add(TeacherDashboardResponse.TeacherTaskDto
                                                                .builder()
                                                                .title("Mark attendance for "
                                                                                + t.getClassRoom().getName() + "-"
                                                                                + t.getClassRoom().getSection())
                                                                .type("Attendance")
                                                                .urgent(true)
                                                                .build()));
                        }
                }

                // 3. Notes Uploaded
                long notesCount = notesRepository.countByUploadedByIdAndIsDeletedFalse(teacherId);

                // 4. Upcoming Online Classes
                long upcomingOnlineCount = onlineClassRepository
                                .countByTeacherIdAndScheduledAtAfterAndIsDeletedFalse(teacherId, now);

                return TeacherDashboardResponse.builder()
                                .classesToday(classesToday)
                                .pendingAttendance(pendingAttendanceCount)
                                .notesUploaded(notesCount)
                                .upcomingOnlineClasses(upcomingOnlineCount)
                                .pendingTasks(pendingTasks)
                                .build();
        }
}
