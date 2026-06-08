package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeInstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeInstallmentPlanRepository extends JpaRepository<FeeInstallmentPlan, Long> {
    List<FeeInstallmentPlan> findBySchoolIdAndAcademicYear(Long schoolId, String academicYear);
}
