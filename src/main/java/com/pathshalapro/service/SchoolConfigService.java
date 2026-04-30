package com.pathshalapro.service;

import com.pathshalapro.entity.School;
import com.pathshalapro.entity.SchoolConfig;
import com.pathshalapro.repository.SchoolConfigRepository;
import com.pathshalapro.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchoolConfigService {

    private final SchoolConfigRepository configRepository;
    private final SchoolRepository schoolRepository;

    public Map<String, String> getConfigs(Long schoolId) {
        List<SchoolConfig> configs = configRepository.findBySchoolId(schoolId);
        Map<String, String> configMap = new HashMap<>();
        for (SchoolConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }

    @Transactional
    public void saveConfig(Long schoolId, String key, String value) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Optional<SchoolConfig> existingConfig = configRepository.findBySchoolIdAndConfigKey(schoolId, key);
        
        if (existingConfig.isPresent()) {
            SchoolConfig config = existingConfig.get();
            config.setConfigValue(value);
            configRepository.save(config);
        } else {
            SchoolConfig config = SchoolConfig.builder()
                    .school(school)
                    .configKey(key)
                    .configValue(value)
                    .build();
            configRepository.save(config);
        }
    }

    @Transactional
    public void saveConfigs(Long schoolId, Map<String, String> configs) {
        configs.forEach((key, value) -> saveConfig(schoolId, key, value));
    }
}
