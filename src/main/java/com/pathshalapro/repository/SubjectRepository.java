package com.pathshalapro.repository;

import com.pathshalapro.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByIdAndSchoolIdAndIsDeletedFalse(Long id, Long schoolId);

    List<Subject> findBySchoolIdAndIsDeletedFalse(Long schoolId);

    Page<Subject> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    List<Subject> findBySchoolIdAndGradeAndIsDeletedFalse(Long schoolId, String grade);

    boolean existsByCodeAndSchoolIdAndIsDeletedFalse(String code, Long schoolId);
    
    Optional<Subject> findByCodeAndSchoolIdAndIsDeletedFalse(String code, Long schoolId);
}
