package com.pathshalapro.service;

import com.pathshalapro.dto.subscription.SubscriptionPlanResponse;
import java.util.List;

public interface SubscriptionService {
    List<SubscriptionPlanResponse> getAllActivePlans();
    List<SubscriptionPlanResponse> getAllPlans();
    SubscriptionPlanResponse getPlanById(Long id);
}
