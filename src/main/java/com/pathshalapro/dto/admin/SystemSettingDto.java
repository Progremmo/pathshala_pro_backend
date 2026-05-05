package com.pathshalapro.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingDto {
    private String configKey;
    private String configValue;
    private String configGroup;
    private String description;
}
