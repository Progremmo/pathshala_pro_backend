package com.pathshalapro.dto.bulkupload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowError {
    private int row;
    private String field;
    private String message;
}
