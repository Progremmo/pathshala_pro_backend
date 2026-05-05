package com.pathshalapro.repository;

import com.pathshalapro.entity.OnlineClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnlineClassRepository extends JpaRepository<OnlineClass, Long> {

    Page<OnlineClass> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    Page<OnlineClass> findByClassRoomIdAndIsDeletedFalse(Long classRoomId, Pageable pageable);

    Page<OnlineClass> findByTeacherIdAndIsDeletedFalse(Long teacherId, Pageable pageable);

    @Query("SELECT oc FROM OnlineClass oc WHERE oc.school.id = :schoolId " +
           "AND oc.scheduledAt BETWEEN :start AND :end " +
           "AND oc.isDeleted = false ORDER BY oc.scheduledAt ASC")
    List<OnlineClass> findUpcomingBySchool(@Param("schoolId") Long schoolId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    long countByTeacherIdAndScheduledAtAfterAndIsDeletedFalse(Long teacherId, LocalDateTime now);

    List<OnlineClass> findByTeacherIdAndScheduledAtAfterAndIsDeletedFalseOrderByScheduledAtAsc(Long teacherId, LocalDateTime now);
}
