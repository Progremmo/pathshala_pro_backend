package com.pathshalapro.repository;

import com.pathshalapro.entity.Timetable;
import com.pathshalapro.entity.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findByClassRoomIdAndAcademicYearAndIsDeletedFalse(Long classRoomId, String academicYear);

    List<Timetable> findByTeacherIdAndAcademicYearAndIsDeletedFalse(Long teacherId, String academicYear);

    List<Timetable> findBySchoolIdAndAcademicYearAndDayOfWeekAndIsDeletedFalse(Long schoolId, String academicYear, DayOfWeek dayOfWeek);
    
    List<Timetable> findBySchoolIdAndDayOfWeekAndIsDeletedFalse(Long schoolId, DayOfWeek dayOfWeek);

    List<Timetable> findByTeacherIdAndDayOfWeekAndAcademicYearAndIsDeletedFalse(Long teacherId, DayOfWeek dayOfWeek, String academicYear);

    /**
     * Conflict detection: Check if teacher is already assigned in an overlapping slot.
     */
    @Query("SELECT COUNT(t) > 0 FROM Timetable t WHERE t.teacher.id = :teacherId AND t.dayOfWeek = :day " +
           "AND t.academicYear = :year AND t.isDeleted = false AND t.id <> :excludeId " +
           "AND NOT (t.endTime <= :startTime OR t.startTime >= :endTime)")
    boolean existsTeacherConflict(@Param("teacherId") Long teacherId,
                                   @Param("day") DayOfWeek day,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("year") String academicYear,
                                   @Param("excludeId") Long excludeId);

    /**
     * Conflict detection: Check if classroom is already booked in an overlapping slot.
     */
    @Query("SELECT COUNT(t) > 0 FROM Timetable t WHERE t.classRoom.id = :classRoomId AND t.dayOfWeek = :day " +
           "AND t.academicYear = :year AND t.isDeleted = false AND t.id <> :excludeId " +
           "AND NOT (t.endTime <= :startTime OR t.startTime >= :endTime)")
    boolean existsClassRoomConflict(@Param("classRoomId") Long classRoomId,
                                     @Param("day") DayOfWeek day,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime,
                                     @Param("year") String academicYear,
                                     @Param("excludeId") Long excludeId);
}
