package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.entity.SystemSetting;
import com.pathshalapro.service.impl.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Platform Settings", description = "Global system configurations managed by Project Admin")
public class SystemSettingController {

    private final SystemSettingService settingService;

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Get all platform settings")
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(settingService.getAllSettings()));
    }

    @GetMapping("/map")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Get all platform settings as key-value map")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSettingsMap() {
        return ResponseEntity.ok(ApiResponse.success(settingService.getSettingsAsMap()));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Update multiple settings at once")
    public ResponseEntity<ApiResponse<Void>> updateSettings(@RequestBody Map<String, String> settings) {
        settingService.updateSettings(settings);
        return ResponseEntity.ok(ApiResponse.success(null, "Settings updated successfully."));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Update a single setting")
    public ResponseEntity<ApiResponse<SystemSetting>> updateSetting(
            @PathVariable String key,
            @RequestParam String value,
            @RequestParam(required = false) String group) {
        return ResponseEntity.ok(ApiResponse.success(settingService.updateSetting(key, value, group)));
    }
}
