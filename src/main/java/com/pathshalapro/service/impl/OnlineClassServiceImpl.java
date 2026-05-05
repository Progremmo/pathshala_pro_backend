package com.pathshalapro.service.impl;

import com.pathshalapro.dto.onlineclass.OnlineClassRequest;
import com.pathshalapro.dto.onlineclass.OnlineClassResponse;
import com.pathshalapro.entity.ClassRoom;
import com.pathshalapro.entity.OnlineClass;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.Subject;
import com.pathshalapro.entity.User;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.ClassRoomRepository;
import com.pathshalapro.repository.OnlineClassRepository;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.repository.SubjectRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OnlineClassServiceImpl {

    private final OnlineClassRepository onlineClassRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public OnlineClassResponse scheduleClass(Long schoolId, OnlineClassRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        ClassRoom classRoom = classRoomRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getClassRoomId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Classroom not found."));

        User teacher = userRepository.findByIdAndIsDeletedFalse(request.getTeacherId())
                .orElseThrow(() -> ApiException.notFound("Teacher not found."));

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                    request.getSubjectId(), schoolId)
                    .orElseThrow(() -> ApiException.notFound("Subject not found."));
        }

        OnlineClass onlineClass = OnlineClass.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .meetingLink(request.getMeetingLink())
                .meetingId(request.getMeetingId())
                .meetingPassword(request.getMeetingPassword())
                .platform(request.getPlatform())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .isRecurring(request.isRecurring())
                .recurrencePattern(request.getRecurrencePattern())
                .status("SCHEDULED")
                .school(school)
                .classRoom(classRoom)
                .subject(subject)
                .teacher(teacher)
                .build();

        return mapToResponse(onlineClassRepository.save(onlineClass));
    }

    @Transactional(readOnly = true)
    public Page<OnlineClassResponse> getClassesBySchool(Long schoolId, Pageable pageable) {
        return onlineClassRepository.findBySchoolIdAndIsDeletedFalse(schoolId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OnlineClassResponse> getClassesByTeacher(Long teacherId, Pageable pageable) {
        return onlineClassRepository.findByTeacherIdAndIsDeletedFalse(teacherId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<OnlineClassResponse> getUpcomingClasses(Long schoolId, int days) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return onlineClassRepository.findUpcomingBySchool(schoolId, start, end)
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public OnlineClassResponse updateStatus(Long schoolId, Long classId, String status) {
        OnlineClass oc = onlineClassRepository.findById(classId)
                .filter(c -> c.getSchool().getId().equals(schoolId) && !c.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Online class not found."));
        oc.setStatus(status);
        return mapToResponse(onlineClassRepository.save(oc));
    }

    @Transactional
    public void deleteClass(Long schoolId, Long classId) {
        OnlineClass oc = onlineClassRepository.findById(classId)
                .filter(c -> c.getSchool().getId().equals(schoolId) && !c.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Online class not found."));
        oc.setDeleted(true);
        onlineClassRepository.save(oc);
    }

    private OnlineClassResponse mapToResponse(OnlineClass oc) {
        return OnlineClassResponse.builder()
                .id(oc.getId())
                .topic(oc.getTitle())
                .description(oc.getDescription())
                .scheduledAt(oc.getScheduledAt())
                .durationMinutes(oc.getDurationMinutes())
                .meetingLink(oc.getMeetingLink())
                .meetingId(oc.getMeetingId())
                .meetingPassword(oc.getMeetingPassword())
                .status(oc.getStatus())
                .classRoomId(oc.getClassRoom().getId())
                .classRoomName(oc.getClassRoom().getName())
                .subjectId(oc.getSubject() != null ? oc.getSubject().getId() : null)
                .subjectName(oc.getSubject() != null ? oc.getSubject().getName() : "General")
                .teacherId(oc.getTeacher().getId())
                .teacherName(oc.getTeacher().getFirstName() + " " + oc.getTeacher().getLastName())
                .build();
    }
}
