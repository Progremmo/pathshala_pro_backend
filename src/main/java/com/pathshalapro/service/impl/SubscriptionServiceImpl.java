package com.pathshalapro.service.impl;

import com.pathshalapro.dto.subscription.SubscriptionPlanResponse;
import com.pathshalapro.entity.SubscriptionPlan;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.SubscriptionPlanRepository;
import com.pathshalapro.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepository planRepository;

    @Override
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        return planRepository.findByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanResponse getPlanById(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Subscription plan not found"));
        return mapToResponse(plan);
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .priceMonthly(plan.getPriceMonthly())
                .priceAnnually(plan.getPriceAnnually())
                .maxStudents(plan.getMaxStudents())
                .maxTeachers(plan.getMaxTeachers())
                .maxClasses(plan.getMaxClasses())
                .storageGb(plan.getStorageGb())
                .active(plan.isActive())
                .build();
    }
}
