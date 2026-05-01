package com.pathshalapro.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeHeadResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isMandatory;
    private LocalDateTime createdAt;
}
