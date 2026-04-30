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
            user.setClassRoom(classRoom);
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
        return userRepository.findStudentsByClassRoomId(classRoomId)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
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
                .classRoomId(user.getClassRoom() != null ? user.getClassRoom().getId() : null)
                .classRoomName(user.getClassRoom() != null ? user.getClassRoom().getName() : null)
                .parentId(user.getParent() != null ? user.getParent().getId() : null)
                .parentName(user.getParent() != null ? (user.getParent().getFirstName() + " " + user.getParent().getLastName()) : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
