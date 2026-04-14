package com.pathshalapro.repository;

import com.pathshalapro.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    @Query("SELECT a FROM Announcement a WHERE a.school.id = :schoolId AND a.isDeleted = false " +
           "AND (a.targetAudience = :audience OR a.targetAudience = 'ALL') " +
           "AND (a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.isPinned DESC, a.createdAt DESC")
    Page<Announcement> findActiveBySchoolAndAudience(@Param("schoolId") Long schoolId,
                                                      @Param("audience") String audience,
                                                      @Param("now") LocalDateTime now,
                                                      Pageable pageable);
}
