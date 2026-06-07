package com.pathshalapro.repository;

import com.pathshalapro.entity.StudentFeeConcession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentFeeConcessionRepository extends JpaRepository<StudentFeeConcession, Long> {
    List<StudentFeeConcession> findByStudentId(Long studentId);
}
