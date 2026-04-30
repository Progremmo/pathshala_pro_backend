package com.pathshalapro.repository;

import com.pathshalapro.entity.SchoolConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolConfigRepository extends JpaRepository<SchoolConfig, Long> {
    List<SchoolConfig> findBySchoolId(Long schoolId);
    Optional<SchoolConfig> findBySchoolIdAndConfigKey(Long schoolId, String configKey);
}
