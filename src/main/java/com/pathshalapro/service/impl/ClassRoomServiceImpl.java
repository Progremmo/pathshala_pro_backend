package com.pathshalapro.service.impl;

import com.pathshalapro.dto.school.ClassRoomResponse;
import com.pathshalapro.entity.ClassRoom;
import com.pathshalapro.repository.ClassRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassRoomServiceImpl {

    private final ClassRoomRepository classRoomRepository;

    public List<ClassRoomResponse> getClassRooms(Long schoolId) {
        return classRoomRepository.findBySchoolIdAndIsDeletedFalse(schoolId)
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
}
