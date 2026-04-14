package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    Page<FeeStructure> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    List<FeeStructure> findBySchoolIdAndGradeAndAcademicYearAndIsDeletedFalse(
            Long schoolId, String grade, String academicYear);

    List<FeeStructure> findBySchoolIdAndAcademicYearAndIsDeletedFalse(Long schoolId, String academicYear);
}
