package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.service.SchoolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/schools/{schoolId}/configs")
@RequiredArgsConstructor
@Tag(name = "School Configuration", description = "Manage school-specific settings and constants")
public class SchoolConfigController {

    private final SchoolConfigService configService;

    @GetMapping
    @Operation(summary = "Get all configurations for a school")
    public ResponseEntity<ApiResponse<Map<String, String>>> getConfigs(@PathVariable Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(configService.getConfigs(schoolId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update multiple configurations for a school")
    public ResponseEntity<ApiResponse<Void>> updateConfigs(
            @PathVariable Long schoolId,
            @RequestBody Map<String, String> configs) {
        configService.saveConfigs(schoolId, configs);
        return ResponseEntity.ok(ApiResponse.success(null, "Configurations updated successfully"));
    }

    @PostMapping("/{key}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update a specific configuration")
    public ResponseEntity<ApiResponse<Void>> updateConfig(
            @PathVariable Long schoolId,
            @PathVariable String key,
            @RequestBody String value) {
        configService.saveConfig(schoolId, key, value);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuration updated successfully"));
    }
}
