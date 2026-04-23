package com.pathshalapro.service;

import com.pathshalapro.dto.user.UserResponse;
import com.pathshalapro.entity.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getUsersByRoleAndSchool(RoleName role, Long schoolId, Pageable pageable);
    UserResponse getUserById(Long id);
}
