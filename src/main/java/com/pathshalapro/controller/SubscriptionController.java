package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.subscription.SubscriptionPlanResponse;
import com.pathshalapro.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription Management", description = "Endpoints for managing subscription plans and school subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    @Operation(summary = "Get all active subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllActivePlans() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getAllActivePlans()));
    }

    @GetMapping("/plans/admin")
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @Operation(summary = "Get all subscription plans (including inactive)")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllPlans() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getAllPlans()));
    }
}
