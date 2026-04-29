package com.pathshalapro.service.impl;

import com.pathshalapro.dto.timetable.TimetableRequest;
import com.pathshalapro.dto.timetable.TimetableResponse;
import com.pathshalapro.entity.*;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Timetable service with conflict detection logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableServiceImpl {

    private final TimetableRepository timetableRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public TimetableResponse createEntry(Long schoolId, TimetableRequest request) {
        // Validate time range
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw ApiException.badRequest("End time must be after start time.");
        }

        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        ClassRoom classRoom = classRoomRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getClassRoomId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Classroom not found."));

        Subject subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getSubjectId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Subject not found."));

        User teacher = userRepository.findByIdAndIsDeletedFalse(request.getTeacherId())
                .orElseThrow(() -> ApiException.notFound("Teacher not found."));

        // ---- Conflict Detection ----
        long excludeId = -1L; // No existing entry to exclude (new creation)

        boolean teacherConflict = timetableRepository.existsTeacherConflict(
                teacher.getId(), request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(),
                request.getAcademicYear(), excludeId);

        if (teacherConflict) {
            throw ApiException.conflict(
                    String.format("Teacher '%s %s' already has a class on %s from %s to %s.",
                            teacher.getFirstName(), teacher.getLastName(),
                            request.getDayOfWeek(), request.getStartTime(), request.getEndTime()));
        }

        boolean classRoomConflict = timetableRepository.existsClassRoomConflict(
                classRoom.getId(), request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(),
                request.getAcademicYear(), excludeId);

        if (classRoomConflict) {
            throw ApiException.conflict(
                    String.format("Classroom '%s' already has a class scheduled on %s from %s to %s.",
                            classRoom.getName(), request.getDayOfWeek(),
                            request.getStartTime(), request.getEndTime()));
        }

        Timetable entry = Timetable.builder()
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .periodNumber(request.getPeriodNumber())
                .academicYear(request.getAcademicYear())
                .school(school)
                .classRoom(classRoom)
                .subject(subject)
                .teacher(teacher)
                .build();

        Timetable saved = timetableRepository.save(entry);
        log.info("Timetable entry created: {} for class {} on {}", saved.getId(),
                classRoom.getName(), request.getDayOfWeek());
        return mapToResponse(saved);
    }

    @Transactional
    public TimetableResponse updateEntry(Long schoolId, Long entryId, TimetableRequest request) {
        Timetable entry = timetableRepository.findById(entryId)
                .filter(t -> t.getSchool().getId().equals(schoolId) && !t.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Timetable entry not found."));

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw ApiException.badRequest("End time must be after start time.");
        }

        // Check conflicts (excluding current entry)
        boolean teacherConflict = timetableRepository.existsTeacherConflict(
                request.getTeacherId(), request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(),
                request.getAcademicYear(), entryId);

        if (teacherConflict) {
            throw ApiException.conflict("Teacher scheduling conflict detected.");
        }

        boolean classRoomConflict = timetableRepository.existsClassRoomConflict(
                request.getClassRoomId(), request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(),
                request.getAcademicYear(), entryId);

        if (classRoomConflict) {
            throw ApiException.conflict("Classroom scheduling conflict detected.");
        }

        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setPeriodNumber(request.getPeriodNumber());

        return mapToResponse(timetableRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> getClassTimetable(Long classRoomId, String academicYear) {
        return timetableRepository.findByClassRoomIdAndAcademicYearAndIsDeletedFalse(classRoomId, academicYear)
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> getTeacherTimetable(Long teacherId, String academicYear) {
        return timetableRepository.findByTeacherIdAndAcademicYearAndIsDeletedFalse(teacherId, academicYear)
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void deleteEntry(Long schoolId, Long entryId) {
        Timetable entry = timetableRepository.findById(entryId)
                .filter(t -> t.getSchool().getId().equals(schoolId) && !t.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Timetable entry not found."));
        entry.setDeleted(true);
        timetableRepository.save(entry);
    }

    private TimetableResponse mapToResponse(Timetable t) {
        return TimetableResponse.builder()
                .id(t.getId())
                .dayOfWeek(t.getDayOfWeek())
                .startTime(t.getStartTime())
                .endTime(t.getEndTime())
                .periodNumber(t.getPeriodNumber())
                .academicYear(t.getAcademicYear())
                .classRoomId(t.getClassRoom().getId())
                .classRoomName(t.getClassRoom().getName())
                .subjectId(t.getSubject().getId())
                .subjectName(t.getSubject().getName())
                .subjectCode(t.getSubject().getCode())
                .teacherId(t.getTeacher().getId())
                .teacherName(t.getTeacher().getFirstName() + " " + t.getTeacher().getLastName())
                .build();
    }
}
