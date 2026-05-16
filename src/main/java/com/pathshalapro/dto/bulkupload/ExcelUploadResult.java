package com.pathshalapro.dto.bulkupload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadResult {
    private int totalRows;
    private int successCount;
    private int failedCount;
    private String module;

    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    public void addError(int row, String field, String message) {
        errors.add(RowError.builder().row(row).field(field).message(message).build());
        failedCount++;
    }

    public void incrementSuccess() {
        successCount++;
    }
}
