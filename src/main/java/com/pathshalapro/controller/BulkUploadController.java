package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.service.impl.BulkUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/schools/{schoolId}/bulk-upload")
@RequiredArgsConstructor
@Tag(name = "Bulk Upload", description = "Excel-based bulk data import for all modules")
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    @GetMapping("/{module}/template")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Download Excel template for a module",
               description = "Supported modules: students, teachers, subjects, classes, timetable, fees")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable Long schoolId,
            @PathVariable String module) {
        byte[] template = bulkUploadService.generateTemplate(module);
        String filename = module + "_template.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    @PostMapping("/{module}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Upload Excel file for bulk import",
               description = "Parses, validates and inserts records. Returns upload summary with errors.")
    public ResponseEntity<ApiResponse<ExcelUploadResult>> uploadExcel(
            @PathVariable Long schoolId,
            @PathVariable String module,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty. Please upload a valid Excel file."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.contains("spreadsheet") && !contentType.contains("excel")
                && !file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid file type. Please upload an Excel file (.xlsx or .xls)."));
        }

        try {
            ExcelUploadResult result = bulkUploadService.processUpload(schoolId, module, file.getInputStream());
            String message = String.format("Upload complete: %d/%d rows imported successfully.",
                    result.getSuccessCount(), result.getTotalRows());
            return ResponseEntity.ok(ApiResponse.success(result, message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
