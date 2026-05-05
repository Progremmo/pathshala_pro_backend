package com.pathshalapro.service.impl;

import com.pathshalapro.entity.SystemSetting;
import com.pathshalapro.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    public List<SystemSetting> getSettingsByGroup(String group) {
        return settingRepository.findByConfigGroup(group);
    }

    public Map<String, String> getSettingsAsMap() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(SystemSetting::getConfigKey, SystemSetting::getConfigValue));
    }

    @Transactional
    public void updateSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            SystemSetting setting = settingRepository.findByConfigKey(key)
                    .orElse(SystemSetting.builder().configKey(key).build());
            
            setting.setConfigValue(value);
            settingRepository.save(setting);
            log.info("Updated system setting: {} = {}", key, value);
        });
    }

    @Transactional
    public SystemSetting updateSetting(String key, String value, String group) {
        SystemSetting setting = settingRepository.findByConfigKey(key)
                .orElse(SystemSetting.builder().configKey(key).configGroup(group).build());
        
        setting.setConfigValue(value);
        if (group != null) {
            setting.setConfigGroup(group);
        }
        return settingRepository.save(setting);
    }
}
