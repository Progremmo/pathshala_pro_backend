package com.pathshalapro.repository;

import com.pathshalapro.entity.LateFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LateFeeRuleRepository extends JpaRepository<LateFeeRule, Long> {
    List<LateFeeRule> findBySchoolId(Long schoolId);
}
