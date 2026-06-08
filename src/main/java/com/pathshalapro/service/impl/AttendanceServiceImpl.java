package com.pathshalapro.service.impl;

import com.pathshalapro.dto.attendance.AttendanceRequest;
import com.pathshalapro.dto.attendance.AttendanceResponse;
import com.pathshalapro.entity.*;
import com.pathshalapro.entity.enums.AttendanceStatus;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Attendance service for daily attendance marking and retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl {

    private final AttendanceRepository attendanceRepository;
    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Bulk attendance marking for a class on a date.
     * Prevents duplicate entries for the same student/date combination.
     */
    @Transactional
    public List<AttendanceResponse> markAttendance(Long schoolId, AttendanceRequest request, User markedBy) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        ClassRoom classRoom = classRoomRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getClassRoomId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Classroom not found."));

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(request.getSubjectId(), schoolId)
                    .orElseThrow(() -> ApiException.notFound("Subject not found."));
        }

        LocalDate attendanceDate = request.getAttendanceDate();

        // Validate: no future dates
        if (attendanceDate.isAfter(LocalDate.now())) {
            throw ApiException.badRequest("Cannot mark attendance for a future date.");
        }

        List<Attendance> savedRecords = new ArrayList<>();

        for (AttendanceRequest.StudentAttendance record : request.getRecords()) {
            User student = userRepository.findByIdAndIsDeletedFalse(record.getStudentId())
                    .orElseThrow(() -> ApiException.notFound("Student not found: " + record.getStudentId()));

            // Check if attendance already marked (update if exists)
            Attendance attendance = attendanceRepository
                    .findByStudentIdAndAttendanceDateAndIsDeletedFalse(student.getId(), attendanceDate)
                    .orElse(Attendance.builder()
                            .school(school)
                            .student(student)
                            .classRoom(classRoom)
                            .markedBy(markedBy)
                            .academicYear(com.pathshalapro.config.AcademicYearContextHolder.get())
                            .build());

            attendance.setAttendanceDate(attendanceDate);
            attendance.setStatus(record.getStatus());
            attendance.setRemarks(record.getRemarks());
            attendance.setSubject(subject);

            savedRecords.add(attendanceRepository.save(attendance));
        }

        log.info("Marked attendance for {} students in class {} on {}",
                savedRecords.size(), classRoom.getName(), attendanceDate);

        return savedRecords.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getClassAttendance(Long schoolId, Long classRoomId, LocalDate date) {
        return attendanceRepository.findByClassRoomIdAndAttendanceDateAndIsDeletedFalse(classRoomId, date)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getStudentAttendance(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentIdAndAttendanceDateBetweenAndIsDeletedFalse(
                studentId, startDate, endDate).stream().map(this::mapToResponse).toList();
    }

    /**
     * Calculate attendance percentage for a student in a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceStats(Long studentId, LocalDate startDate, LocalDate endDate) {
        Long total = attendanceRepository.countTotalDaysByStudent(studentId, startDate, endDate);
        Long present = attendanceRepository.countByStudentAndStatusAndDateRange(
                studentId, AttendanceStatus.PRESENT, startDate, endDate);
        Long absent = attendanceRepository.countByStudentAndStatusAndDateRange(
                studentId, AttendanceStatus.ABSENT, startDate, endDate);
        Long late = attendanceRepository.countByStudentAndStatusAndDateRange(
                studentId, AttendanceStatus.LATE, startDate, endDate);

        double percentage = total > 0 ? (double) (present + late) / total * 100 : 0.0;

        return Map.of(
                "totalDays", total,
                "present", present,
                "absent", absent,
                "late", late,
                "attendancePercentage", Math.round(percentage * 100.0) / 100.0
        );
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentName(a.getStudent().getFirstName() + " " + a.getStudent().getLastName())
                .classRoomId(a.getClassRoom().getId())
                .classRoomName(a.getClassRoom().getName())
                .attendanceDate(a.getAttendanceDate())
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .build();
    }
}
