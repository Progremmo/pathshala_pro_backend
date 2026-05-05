package com.pathshalapro.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardResponse {
    private long classesToday;
    private long pendingAttendance;
    private long notesUploaded;
    private long upcomingOnlineClasses;
    private List<TeacherTaskDto> pendingTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherTaskDto {
        private String title;
        private String type; // e.g., "Attendance", "Marks", "Notes"
        private boolean urgent;
    }
}
