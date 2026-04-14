package com.pathshalapro.repository;

import com.pathshalapro.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School> findByCodeAndIsDeletedFalse(String code);

    Optional<School> findByIdAndIsDeletedFalse(Long id);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);

    Page<School> findByIsDeletedFalse(Pageable pageable);

    Page<School> findByIsActiveTrueAndIsDeletedFalse(Pageable pageable);

    @Query("SELECT s FROM School s WHERE s.isDeleted = false AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<School> searchSchools(@Param("search") String search, Pageable pageable);
}
