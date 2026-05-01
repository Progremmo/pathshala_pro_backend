package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeeGroupRepository extends JpaRepository<FeeGroup, Long> {
    List<FeeGroup> findBySchoolIdAndIsDeletedFalse(Long schoolId);
}
