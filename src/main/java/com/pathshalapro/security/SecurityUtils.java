package com.pathshalapro.security;

import com.pathshalapro.entity.User;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility to get currently authenticated user from the security context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Returns the currently authenticated User entity.
     * Throws IllegalStateException if no authenticated user is found.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context.");
        }
        String email = authentication.getName();
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + email));
    }

    /**
     * Returns the email of the currently authenticated user.
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Checks if the current user has a specific role.
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isProjectAdmin() {
        return hasRole("PROJECT_ADMIN");
    }

    public boolean isSchoolAdmin() {
        return hasRole("SCHOOL_ADMIN");
    }

    public boolean isTeacher() {
        return hasRole("TEACHER");
    }
}
