package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeAuditLogRepository extends JpaRepository<FeeAuditLog, Long> {
    List<FeeAuditLog> findBySchoolIdOrderByTimestampDesc(Long schoolId);
}
