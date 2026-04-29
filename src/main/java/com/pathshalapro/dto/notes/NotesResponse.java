package com.pathshalapro.dto.notes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesResponse {
    private Long id;
    private String title;
    private String description;
    private String contentUrl;
    private String contentType;
    private String grade;
    private String academicYear;
    private boolean isVisible;
    
    private Long subjectId;
    private String subjectName;
    
    private Long uploadedById;
    private String uploadedByName;
    
    private LocalDateTime createdAt;
}
