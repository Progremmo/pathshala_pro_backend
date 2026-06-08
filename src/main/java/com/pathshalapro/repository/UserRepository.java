package com.pathshalapro.repository;

import com.pathshalapro.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByEmail(String email);
    
    Page<User> findByIsDeletedFalse(Pageable pageable);

    Page<User> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.school.id = :schoolId AND r.name = :roleName AND u.isDeleted = false")
    Page<User> findBySchoolIdAndRoleName(@Param("schoolId") Long schoolId,
                                          @Param("roleName") com.pathshalapro.entity.enums.RoleName roleName,
                                          Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isDeleted = false")
    Page<User> findAllByRoleName(@Param("roleName") com.pathshalapro.entity.enums.RoleName roleName,
                                 Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.school.id = :schoolId AND r.name = :roleName AND u.isDeleted = false")
    List<User> findAllBySchoolIdAndRoleName(@Param("schoolId") Long schoolId,
                                             @Param("roleName") com.pathshalapro.entity.enums.RoleName roleName);

    @Query("SELECT sca.student FROM StudentClassAllocation sca WHERE sca.classRoom.id = :classRoomId AND sca.academicYear = :academicYear AND sca.student.isDeleted = false")
    List<User> findStudentsByClassRoomIdAndAcademicYear(@Param("classRoomId") Long classRoomId, @Param("academicYear") String academicYear);

    @Query("SELECT u FROM User u WHERE u.parent.id = :parentId AND u.isDeleted = false")
    List<User> findChildrenByParentId(@Param("parentId") Long parentId);

    Optional<User> findByAdmissionNoAndSchoolIdAndIsDeletedFalse(String admissionNo, Long schoolId);

    Page<User> findBySchoolIdAndIsActiveTrueAndIsDeletedFalse(Long schoolId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.school.id = :schoolId AND r.name = :roleName " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) AND u.isDeleted = false")
    Page<User> searchBySchoolAndRole(@Param("schoolId") Long schoolId,
                                      @Param("roleName") com.pathshalapro.entity.enums.RoleName roleName,
                                      @Param("search") String search,
                                      Pageable pageable);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE " +
           "(:roleName IS NULL OR r.name = :roleName) AND " +
           "(:schoolId IS NULL OR u.school.id = :schoolId) AND " +
           "(CAST(:search AS String) IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) AND " +
           "u.isDeleted = false")
    Page<User> searchUsers(@Param("roleName") com.pathshalapro.entity.enums.RoleName roleName,
                           @Param("schoolId") Long schoolId,
                           @Param("search") String search,
                           Pageable pageable);
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.school.id = :schoolId AND r.name = :roleName AND u.isDeleted = false")
    long countBySchoolIdAndRoleName(@Param("schoolId") Long schoolId, @Param("roleName") com.pathshalapro.entity.enums.RoleName roleName);
}
