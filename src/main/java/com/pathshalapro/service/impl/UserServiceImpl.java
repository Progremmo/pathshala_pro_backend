package com.pathshalapro.service.impl;

import com.pathshalapro.dto.user.UserUpdateRequest;
import com.pathshalapro.dto.user.UserResponse;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.ClassRoom;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.UserRepository;
import com.pathshalapro.repository.ClassRoomRepository;
import com.pathshalapro.security.SecurityUtils;
import com.pathshalapro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SecurityUtils securityUtils;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    public Page<UserResponse> getUsersByRoleAndSchool(RoleName role, Long schoolId, String search, Pageable pageable) {
        // Security check: School Admin can only see users from their own school
        if (securityUtils.isSchoolAdmin()) {
            User currentUser = securityUtils.getCurrentUser();
            schoolId = currentUser.getSchool().getId();
        }
        
        Page<User> users = userRepository.searchUsers(role, schoolId, search, pageable);
        return users.map(this::mapToUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + id));

        // Update basic fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        if (request.getActive() != null) user.setActive(request.getActive());

        // Role-specific updates
        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = classRoomRepository.findByIdAndIsDeletedFalse(request.getClassRoomId())
                    .orElseThrow(() -> ApiException.notFound("Classroom not found"));
            
            String currentAcademicYear = com.pathshalapro.config.AcademicYearContextHolder.get();
            if (user.getClassAllocations() == null) {
                user.setClassAllocations(new java.util.ArrayList<>());
            }
            
            boolean allocationExists = user.getClassAllocations().stream()
                .anyMatch(a -> a.getAcademicYear().equals(currentAcademicYear));
                
            if (!allocationExists) {
                com.pathshalapro.entity.StudentClassAllocation allocation = com.pathshalapro.entity.StudentClassAllocation.builder()
                    .student(user)
                    .classRoom(classRoom)
                    .academicYear(currentAcademicYear)
                    .school(user.getSchool())
                    .build();
                user.getClassAllocations().add(allocation);
            } else {
                user.getClassAllocations().stream()
                    .filter(a -> a.getAcademicYear().equals(currentAcademicYear))
                    .findFirst()
                    .ifPresent(a -> a.setClassRoom(classRoom));
            }
        }

        if (request.getAdmissionNo() != null) user.setAdmissionNo(request.getAdmissionNo());
        if (request.getEmployeeId() != null) user.setEmployeeId(request.getEmployeeId());
        if (request.getQualification() != null) user.setQualification(request.getQualification());
        if (request.getJoiningDate() != null) user.setJoiningDate(request.getJoiningDate());

        if (request.getParentId() != null) {
            User parent = userRepository.findByIdAndIsDeletedFalse(request.getParentId())
                    .orElseThrow(() -> ApiException.notFound("Parent user not found"));
            user.setParent(parent);
        }

        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse toggleStatus(Long id, boolean active) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + id));
        user.setActive(active);
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public java.util.List<UserResponse> getStudentsByClass(Long classRoomId) {
        String currentAcademicYear = com.pathshalapro.config.AcademicYearContextHolder.get();
        return userRepository.findStudentsByClassRoomIdAndAcademicYear(classRoomId, currentAcademicYear)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<UserResponse> getChildrenByParentId(Long parentId) {
        return userRepository.findChildrenByParentId(parentId)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        String currentAcademicYear = com.pathshalapro.config.AcademicYearContextHolder.get();
        ClassRoom currentClass = user.getClassAllocations() == null ? null : user.getClassAllocations().stream()
                .filter(a -> a.getAcademicYear().equals(currentAcademicYear))
                .map(com.pathshalapro.entity.StudentClassAllocation::getClassRoom)
                .findFirst().orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePicUrl(user.getProfilePicUrl())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .admissionNo(user.getAdmissionNo())
                .employeeId(user.getEmployeeId())
                .qualification(user.getQualification())
                .joiningDate(user.getJoiningDate())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
                .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
                .schoolName(user.getSchool() != null ? user.getSchool().getName() : null)
                .classRoomId(currentClass != null ? currentClass.getId() : null)
                .classRoomName(currentClass != null ? currentClass.getName() : null)
                .parentId(user.getParent() != null ? user.getParent().getId() : null)
                .parentName(user.getParent() != null ? (user.getParent().getFirstName() + " " + user.getParent().getLastName()) : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + id));

        // Soft delete the user
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);

        // Cascade soft delete to student-related records
        entityManager.createQuery("UPDATE Attendance a SET a.isDeleted = true WHERE a.student.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE Marks m SET m.isDeleted = true WHERE m.student.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE FeeInvoice f SET f.isDeleted = true WHERE f.student.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE Payment p SET p.isDeleted = true WHERE p.paidBy.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE StudentFeeConcession c SET c.isDeleted = true WHERE c.student.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE AdvanceCredit ac SET ac.isDeleted = true WHERE ac.student.id = :userId")
                .setParameter("userId", id).executeUpdate();

        // Cascade soft delete to teacher-related records
        entityManager.createQuery("UPDATE Timetable t SET t.isDeleted = true WHERE t.teacher.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE Notes n SET n.isDeleted = true WHERE n.teacher.id = :userId")
                .setParameter("userId", id).executeUpdate();
        entityManager.createQuery("UPDATE OnlineClass oc SET oc.isDeleted = true WHERE oc.teacher.id = :userId")
                .setParameter("userId", id).executeUpdate();
    }
}
