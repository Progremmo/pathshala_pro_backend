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

    List<ClassRoom> findBySchoolIdAndIsDeletedFalse(Long schoolId);

    Page<ClassRoom> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    List<ClassRoom> findBySchoolIdAndGradeAndAcademicYearAndIsDeletedFalse(
            Long schoolId, String grade, String academicYear);

    @Query("SELECT COUNT(u) FROM User u WHERE u.classRoom.id = :classRoomId AND u.isDeleted = false")
    Long countStudentsByClassRoomId(@Param("classRoomId") Long classRoomId);

    boolean existsByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(
            String name, String section, Long schoolId, String academicYear);
}
