package com.pathshalapro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing configuration - provides current auditor for created_by/updated_by fields.
 */
@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return Optional.of(0L); // System/seed data
            }
            // In a real implementation, resolve user ID from the username
            // For now, return a placeholder
            return Optional.of(1L);
        };
    }
}
