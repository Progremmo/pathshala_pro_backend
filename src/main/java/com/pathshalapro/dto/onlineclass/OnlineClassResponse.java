package com.pathshalapro.dto.onlineclass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineClassResponse {
    private Long id;
    private String topic;
    private String description;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingLink;
    private String meetingId;
    private String meetingPassword;
    private String status;
    
    private Long classRoomId;
    private String classRoomName;
    
    private Long subjectId;
    private String subjectName;
    
    private Long teacherId;
    private String teacherName;
}
