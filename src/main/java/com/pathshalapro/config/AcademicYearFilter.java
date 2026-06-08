package com.pathshalapro.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class AcademicYearFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Academic-Year";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String academicYear = request.getHeader(HEADER_NAME);
        
        if (academicYear != null && !academicYear.isBlank()) {
            AcademicYearContextHolder.set(academicYear);
        } else {
            // Defaulting for non-authenticated or public endpoints
            // Could be overridden or validated per school
            // Since this is a massive change, we'll gracefully fallback or leave it null.
            // Some endpoints might not need it, others will throw exception in service.
            AcademicYearContextHolder.set("2026-27"); // Using default from DataSeeder for fallback
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            AcademicYearContextHolder.clear();
        }
    }
}
