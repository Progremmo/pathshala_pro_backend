package com.pathshalapro.dto.onlineclass;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnlineClassRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String meetingLink;
    private String meetingId;
    private String meetingPassword;
    private String platform; // AGORA, ZOOM, GOOGLE_MEET

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    private Integer durationMinutes;
    private boolean isRecurring = false;
    private String recurrencePattern;

    @NotNull(message = "Class room ID is required")
    private Long classRoomId;

    private Long subjectId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
}
