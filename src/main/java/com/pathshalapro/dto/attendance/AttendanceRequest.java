package com.pathshalapro.dto.attendance;

import com.pathshalapro.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceRequest {

    @NotNull(message = "Class room ID is required")
    private Long classRoomId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private Long subjectId; // Optional: for per-subject attendance

    @NotNull(message = "Attendance records are required")
    private List<StudentAttendance> records;

    @Data
    public static class StudentAttendance {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Status is required")
        private AttendanceStatus status;

        private String remarks;
    }
}
