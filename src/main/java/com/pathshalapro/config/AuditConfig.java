package com.pathshalapro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import com.pathshalapro.security.UserPrincipal;

/**
 * JPA Auditing configuration - provides current auditor for
 * created_by/updated_by fields.
 */
@Configuration
@SuppressWarnings("null")
public class AuditConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new AuditorAware<Long>() {
            @Override
            public Optional<Long> getCurrentAuditor() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                    return Optional.of(0L); // System/seed data
                }

                if (auth.getPrincipal() instanceof UserPrincipal principal) {
                    return Optional.of(principal.getId());
                }

                return Optional.of(0L);
            }
        };
    }
}
