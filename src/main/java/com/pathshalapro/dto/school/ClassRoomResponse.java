package com.pathshalapro.dto.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomResponse {
    private Long id;
    private String name;
    private String section;
    private String grade;
    private String academicYear;
    private Integer capacity;
    private String roomNumber;
    private Long schoolId;
    private Long classTeacherId;
    private String classTeacherName;
}
