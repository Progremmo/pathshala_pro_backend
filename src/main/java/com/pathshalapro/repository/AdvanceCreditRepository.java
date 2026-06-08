package com.pathshalapro.repository;

import com.pathshalapro.entity.AdvanceCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvanceCreditRepository extends JpaRepository<AdvanceCredit, Long> {
    Optional<AdvanceCredit> findByStudentId(Long studentId);
}
