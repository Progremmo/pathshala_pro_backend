package com.pathshalapro.dto.timetable;

import com.pathshalapro.entity.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer periodNumber;
    private String academicYear;
    
    private Long classRoomId;
    private String classRoomName;
    
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    
    private Long teacherId;
    private String teacherName;
}
