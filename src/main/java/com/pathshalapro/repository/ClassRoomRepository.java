package com.pathshalapro.repository;

import com.pathshalapro.entity.ClassRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    Optional<ClassRoom> findByIdAndIsDeletedFalse(Long id);
    
    Optional<ClassRoom> findByIdAndSchoolIdAndIsDeletedFalse(Long id, Long schoolId);
    
    Optional<ClassRoom> findByIdAndSchoolIdAndAcademicYearAndIsDeletedFalse(Long id, Long schoolId, String academicYear);

    List<ClassRoom> findBySchoolIdAndAcademicYearAndIsDeletedFalse(Long schoolId, String academicYear);

    Page<ClassRoom> findBySchoolIdAndAcademicYearAndIsDeletedFalse(Long schoolId, String academicYear, Pageable pageable);

    List<ClassRoom> findBySchoolIdAndGradeAndAcademicYearAndIsDeletedFalse(
            Long schoolId, String grade, String academicYear);

    @Query("SELECT COUNT(sca) FROM StudentClassAllocation sca WHERE sca.classRoom.id = :classRoomId AND sca.student.isDeleted = false")
    Long countStudentsByClassRoomId(@Param("classRoomId") Long classRoomId);

    boolean existsByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(
            String name, String section, Long schoolId, String academicYear);

    Optional<ClassRoom> findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(
            String name, String section, Long schoolId, String academicYear);
}
