package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeeAllocationRepository extends JpaRepository<FeeAllocation, Long> {
    List<FeeAllocation> findBySchoolIdAndIsDeletedFalse(Long schoolId);
    List<FeeAllocation> findByClassRoomIdAndAcademicYearAndIsDeletedFalse(Long classRoomId, String academicYear);
    List<FeeAllocation> findByStudentIdAndAcademicYearAndIsDeletedFalse(Long studentId, String academicYear);
}
