package com.pathshalapro.repository;

import com.pathshalapro.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    Optional<SystemSetting> findByConfigKey(String configKey);
    List<SystemSetting> findByConfigGroup(String configGroup);
}
