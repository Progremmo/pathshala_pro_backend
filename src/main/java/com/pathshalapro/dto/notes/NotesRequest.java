package com.pathshalapro.dto.notes;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NotesRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotBlank(message = "Content URL is required")
    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String contentUrl;

    @NotBlank(message = "Content type is required")
    private String contentType; // PDF, VIDEO, LINK, DOC

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private String grade;
    private String academicYear;
    private boolean isVisible = true;
}
