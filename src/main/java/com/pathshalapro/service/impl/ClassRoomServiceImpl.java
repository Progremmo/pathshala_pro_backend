package com.pathshalapro.service.impl;

import com.pathshalapro.dto.school.ClassRoomResponse;
import com.pathshalapro.dto.school.ClassRoomRequest;
import com.pathshalapro.entity.ClassRoom;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.User;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.ClassRoomRepository;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class ClassRoomServiceImpl {

    private final ClassRoomRepository classRoomRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    public List<ClassRoomResponse> getClassRooms(Long schoolId) {
        String currentAcademicYear = com.pathshalapro.config.AcademicYearContextHolder.get();
        return classRoomRepository.findBySchoolIdAndAcademicYearAndIsDeletedFalse(schoolId, currentAcademicYear)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ClassRoomResponse mapToResponse(ClassRoom classRoom) {
        return ClassRoomResponse.builder()
                .id(classRoom.getId())
                .name(classRoom.getName())
                .section(classRoom.getSection())
                .grade(classRoom.getGrade())
                .academicYear(classRoom.getAcademicYear())
                .capacity(classRoom.getCapacity())
                .roomNumber(classRoom.getRoomNumber())
                .schoolId(classRoom.getSchool().getId())
                .classTeacherId(classRoom.getClassTeacher() != null ? classRoom.getClassTeacher().getId() : null)
                .classTeacherName(classRoom.getClassTeacher() != null ?
                        (classRoom.getClassTeacher().getFirstName() + " " + classRoom.getClassTeacher().getLastName()) : null)
                .build();
    }

    @Transactional
    public ClassRoomResponse createClassRoom(Long schoolId, ClassRoomRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found with id: " + schoolId));

        String currentAcademicYear = com.pathshalapro.config.AcademicYearContextHolder.get();

        User teacher = null;
        if (request.getClassTeacherId() != null) {
            teacher = userRepository.findByIdAndIsDeletedFalse(request.getClassTeacherId())
                    .orElseThrow(() -> ApiException.notFound("Teacher not found with id: " + request.getClassTeacherId()));
        }

        ClassRoom classRoom = ClassRoom.builder()
                .name(request.getName())
                .section(request.getSection())
                .grade(request.getGrade())
                .academicYear(currentAcademicYear) // Enforce context
                .capacity(request.getCapacity())
                .roomNumber(request.getRoomNumber())
                .school(school)
                .classTeacher(teacher)
                .build();

        return mapToResponse(classRoomRepository.save(classRoom));
    }

    @Transactional
    public ClassRoomResponse updateClassRoom(Long schoolId, Long classRoomId, ClassRoomRequest request) {
        ClassRoom classRoom = classRoomRepository.findByIdAndIsDeletedFalse(classRoomId)
                .orElseThrow(() -> ApiException.notFound("ClassRoom not found with id: " + classRoomId));

        if (!classRoom.getSchool().getId().equals(schoolId)) {
            throw ApiException.badRequest("Classroom does not belong to the specified school");
        }

        String currentAcademicYear = com.pathshalapro.config.AcademicYearContextHolder.get();
        if (!classRoom.getAcademicYear().equals(currentAcademicYear)) {
            throw ApiException.badRequest("Cannot modify a classroom from a different academic year.");
        }

        User teacher = null;
        if (request.getClassTeacherId() != null) {
            teacher = userRepository.findByIdAndIsDeletedFalse(request.getClassTeacherId())
                    .orElseThrow(() -> ApiException.notFound("Teacher not found with id: " + request.getClassTeacherId()));
        }

        classRoom.setName(request.getName());
        classRoom.setSection(request.getSection());
        classRoom.setGrade(request.getGrade());
        // Do not allow changing the academic year on update
        classRoom.setCapacity(request.getCapacity());
        classRoom.setRoomNumber(request.getRoomNumber());
        classRoom.setClassTeacher(teacher);

        return mapToResponse(classRoomRepository.save(classRoom));
    }

    @Transactional
    public void deleteClassRoom(Long schoolId, Long classRoomId) {
        ClassRoom classRoom = classRoomRepository.findByIdAndIsDeletedFalse(classRoomId)
                .orElseThrow(() -> ApiException.notFound("ClassRoom not found with id: " + classRoomId));

        if (!classRoom.getSchool().getId().equals(schoolId)) {
            throw ApiException.badRequest("Classroom does not belong to the specified school");
        }

        classRoom.setDeleted(true);
        classRoomRepository.save(classRoom);
    }
}
