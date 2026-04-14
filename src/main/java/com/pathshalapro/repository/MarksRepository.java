package com.pathshalapro.repository;

import com.pathshalapro.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {

    Optional<Marks> findByExamIdAndStudentIdAndIsDeletedFalse(Long examId, Long studentId);

    List<Marks> findByExamIdAndIsDeletedFalse(Long examId);

    List<Marks> findByStudentIdAndIsDeletedFalse(Long studentId);

    @Query("SELECT m FROM Marks m WHERE m.student.id = :studentId AND m.exam.classRoom.id = :classRoomId " +
           "AND m.exam.academicYear = :year AND m.isDeleted = false")
    List<Marks> findStudentResultsByClassAndYear(@Param("studentId") Long studentId,
                                                  @Param("classRoomId") Long classRoomId,
                                                  @Param("year") String year);

    @Query("SELECT AVG(m.marksObtained) FROM Marks m WHERE m.exam.id = :examId AND m.isDeleted = false AND m.isAbsent = false")
    Double getAverageMarksByExam(@Param("examId") Long examId);

    @Query("SELECT m FROM Marks m WHERE m.exam.subject.id = :subjectId AND m.student.id = :studentId AND m.isDeleted = false")
    List<Marks> findBySubjectAndStudent(@Param("subjectId") Long subjectId, @Param("studentId") Long studentId);
}
