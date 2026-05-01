package com.pathshalapro.service.impl;

import com.pathshalapro.dto.school.SchoolRequest;
import com.pathshalapro.dto.school.SchoolResponse;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.SchoolSubscription;
import com.pathshalapro.entity.enums.SubscriptionStatus;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.repository.SchoolSubscriptionRepository;
import com.pathshalapro.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * School management service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl {

    private final SchoolRepository schoolRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    @Transactional
    public SchoolResponse createSchool(SchoolRequest request) {
        if (schoolRepository.existsByCode(request.getCode())) {
            throw ApiException.conflict("School with code '" + request.getCode() + "' already exists.");
        }

        if (request.getEmail() != null && schoolRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("School with email '" + request.getEmail() + "' already exists.");
        }

        School school = School.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .logoUrl(request.getLogoUrl())
                .isActive(true)
                .subscriptionStatus(SubscriptionStatus.TRIAL)
                .build();

        School saved = schoolRepository.save(school);

        // Auto-assign a TRIAL subscription using the first available plan
        planRepository.findByIsActiveTrueAndIsDeletedFalse().stream().findFirst()
                .ifPresent(plan -> {
                    SchoolSubscription trial = SchoolSubscription.builder()
                            .school(saved)
                            .plan(plan)
                            .status(SubscriptionStatus.TRIAL)
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now().plusDays(30)) // 30-day trial
                            .trialEndDate(LocalDate.now().plusDays(30))
                            .billingCycle("MONTHLY")
                            .autoRenew(false)
                            .build();
                    subscriptionRepository.save(trial);
                });

        log.info("School created: {} [{}]", saved.getName(), saved.getCode());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SchoolResponse getSchoolById(Long id) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("School not found: " + id));
        return mapToResponse(school);
    }

    @Transactional(readOnly = true)
    public Page<SchoolResponse> getAllSchools(Pageable pageable) {
        return schoolRepository.findByIsDeletedFalse(pageable).map(this::mapToResponse);
    }

    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("School not found: " + id));

        // Check code uniqueness (only if changed)
        if (!school.getCode().equals(request.getCode()) && schoolRepository.existsByCode(request.getCode())) {
            throw ApiException.conflict("School code already in use: " + request.getCode());
        }

        school.setName(request.getName());
        school.setCode(request.getCode());
        school.setAddress(request.getAddress());
        school.setCity(request.getCity());
        school.setState(request.getState());
        school.setPincode(request.getPincode());
        school.setPhone(request.getPhone());
        school.setEmail(request.getEmail());
        school.setWebsite(request.getWebsite());
        school.setLogoUrl(request.getLogoUrl());
        school.setActive(request.isActive());

        return mapToResponse(schoolRepository.save(school));
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("School not found: " + id));
        school.setDeleted(true);
        school.setActive(false);
        schoolRepository.save(school);
        log.info("School soft-deleted: {}", id);
    }

    private SchoolResponse mapToResponse(School school) {
        return SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .code(school.getCode())
                .address(school.getAddress())
                .city(school.getCity())
                .state(school.getState())
                .pincode(school.getPincode())
                .phone(school.getPhone())
                .email(school.getEmail())
                .website(school.getWebsite())
                .logoUrl(school.getLogoUrl())
                .active(school.isActive())
                .subscriptionStatus(school.getSubscriptionStatus())
                .createdAt(school.getCreatedAt())
                .build();
    }
}
