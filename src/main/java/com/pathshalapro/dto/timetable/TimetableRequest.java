package com.pathshalapro.dto.timetable;

import com.pathshalapro.entity.enums.DayOfWeek;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimetableRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private Integer periodNumber;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Class room ID is required")
    private Long classRoomId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
}
