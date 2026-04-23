package com.pathshalapro.service.impl;

import com.pathshalapro.dto.user.UserResponse;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.UserRepository;
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

    @Override
    public Page<UserResponse> getUsersByRoleAndSchool(RoleName role, Long schoolId, Pageable pageable) {
        Page<User> users;
        if (schoolId != null) {
            users = userRepository.findBySchoolIdAndRoleName(schoolId, role, pageable);
        } else {
            users = userRepository.findAllByRoleName(role, pageable);
        }
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
    public UserResponse toggleStatus(Long id, boolean active) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + id));
        user.setActive(active);
        return mapToUserResponse(userRepository.save(user));
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
                .isActive(user.isActive())
                .isEmailVerified(user.isEmailVerified())
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
