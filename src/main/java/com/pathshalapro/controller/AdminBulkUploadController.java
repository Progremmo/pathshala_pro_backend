package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.service.bulkupload.SchoolExcelParser;
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
@RequestMapping("/admin/bulk-upload")
@RequiredArgsConstructor
@Tag(name = "Admin Bulk Upload", description = "Project-admin level bulk import for schools")
public class AdminBulkUploadController {

    private final SchoolExcelParser schoolExcelParser;

    @GetMapping("/schools/template")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Download School + Admin Excel template",
               description = "Downloads a template with columns for school details and admin user details")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = schoolExcelParser.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schools_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    @PostMapping("/schools")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Bulk import schools with administrators",
               description = "Each row creates a School and its SCHOOL_ADMIN user. Returns upload summary with errors.")
    public ResponseEntity<ApiResponse<ExcelUploadResult>> uploadSchools(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty. Please upload a valid Excel file."));
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        if (contentType == null || (!contentType.contains("spreadsheet") && !contentType.contains("excel")
                && (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid file type. Please upload an Excel file (.xlsx or .xls)."));
        }

        try {
            ExcelUploadResult result = schoolExcelParser.parse(file.getInputStream());
            String message = String.format("Upload complete: %d/%d schools created successfully.",
                    result.getSuccessCount(), result.getTotalRows());
            return ResponseEntity.ok(ApiResponse.success(result, message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
