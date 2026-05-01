package com.pathshalapro.repository;

import com.pathshalapro.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    Page<Exam> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    List<Exam> findByClassRoomIdAndAcademicYearAndIsDeletedFalse(Long classRoomId, String academicYear);

    List<Exam> findBySchoolIdAndExamDateBetweenAndIsDeletedFalse(Long schoolId, LocalDate start, LocalDate end);

    @Query("SELECT e FROM Exam e WHERE e.classRoom.id = :classRoomId AND e.isResultPublished = true AND e.isDeleted = false")
    List<Exam> findPublishedResultsByClassRoom(@Param("classRoomId") Long classRoomId);

    Page<Exam> findBySchoolIdAndClassRoomIdAndIsDeletedFalse(Long schoolId, Long classRoomId, Pageable pageable);

    boolean existsBySchoolIdAndNameAndIsDeletedFalse(Long schoolId, String name);
}
