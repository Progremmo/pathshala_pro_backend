package com.pathshalapro.dto.attendance;

import com.pathshalapro.entity.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long classRoomId;
    private String classRoomName;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String remarks;
}
