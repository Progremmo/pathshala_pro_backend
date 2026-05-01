package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeeHeadRepository extends JpaRepository<FeeHead, Long> {
    List<FeeHead> findBySchoolIdAndIsDeletedFalse(Long schoolId);
    long countBySchoolIdAndIsDeletedFalse(Long schoolId);
}
