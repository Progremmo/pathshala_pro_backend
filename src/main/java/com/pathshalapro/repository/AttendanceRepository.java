package com.pathshalapro.repository;

import com.pathshalapro.entity.Attendance;
import com.pathshalapro.entity.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Check if attendance already marked
    Optional<Attendance> findByStudentIdAndAttendanceDateAndIsDeletedFalse(Long studentId, LocalDate date);

    // Get attendance for a class on a date
    List<Attendance> findByClassRoomIdAndAttendanceDateAndIsDeletedFalse(Long classRoomId, LocalDate date);

    // Get attendance for a student in a date range
    List<Attendance> findByStudentIdAndAttendanceDateBetweenAndIsDeletedFalse(
            Long studentId, LocalDate startDate, LocalDate endDate);

    // Attendance report for a class in a month
    @Query("SELECT a FROM Attendance a WHERE a.school.id = :schoolId AND a.classRoom.id = :classRoomId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    List<Attendance> findClassAttendance(@Param("schoolId") Long schoolId,
                                          @Param("classRoomId") Long classRoomId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // Count present/absent for a student
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.status = :status " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    Long countByStudentAndStatusAndDateRange(@Param("studentId") Long studentId,
                                             @Param("status") AttendanceStatus status,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // School-level attendance for a day
    List<Attendance> findBySchoolIdAndAcademicYearAndAttendanceDateAndIsDeletedFalse(Long schoolId, String academicYear, LocalDate date);

    Page<Attendance> findByStudentIdAndIsDeletedFalse(Long studentId, Pageable pageable);

    Page<Attendance> findByStudentIdAndAcademicYearAndIsDeletedFalse(Long studentId, String academicYear, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.isDeleted = false AND " +
           "a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countTotalDaysByStudent(@Param("studentId") Long studentId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.school.id = :schoolId AND a.attendanceDate = :date AND a.status = :status AND a.isDeleted = false")
    long countBySchoolIdAndDateAndStatus(@Param("schoolId") Long schoolId, @Param("date") LocalDate date, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.school.id = :schoolId AND a.attendanceDate = :date AND a.isDeleted = false")
    long countBySchoolIdAndDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);

    long countByStudentIdAndAcademicYearAndIsDeletedFalse(Long studentId, String academicYear);

    boolean existsByClassRoomIdAndAttendanceDateAndIsDeletedFalse(Long classRoomId, LocalDate date);
}
