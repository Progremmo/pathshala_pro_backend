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

    Page<Exam> findBySchoolIdAndAcademicYearAndIsDeletedFalse(Long schoolId, String academicYear, Pageable pageable);

    List<Exam> findByClassRoomIdAndAcademicYearAndIsDeletedFalse(Long classRoomId, String academicYear);

    List<Exam> findBySchoolIdAndAcademicYearAndExamDateBetweenAndIsDeletedFalse(Long schoolId, String academicYear, LocalDate start, LocalDate end);

    @Query("SELECT e FROM Exam e WHERE e.classRoom.id = :classRoomId AND e.academicYear = :academicYear AND e.isResultPublished = true AND e.isDeleted = false")
    List<Exam> findPublishedResultsByClassRoomAndAcademicYear(@Param("classRoomId") Long classRoomId, @Param("academicYear") String academicYear);

    Page<Exam> findBySchoolIdAndClassRoomIdAndAcademicYearAndIsDeletedFalse(Long schoolId, Long classRoomId, String academicYear, Pageable pageable);

    Page<Exam> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);
    
    boolean existsBySchoolIdAndNameAndIsDeletedFalse(Long schoolId, String name);

    boolean existsBySchoolIdAndAcademicYearAndNameAndIsDeletedFalse(Long schoolId, String academicYear, String name);
}
