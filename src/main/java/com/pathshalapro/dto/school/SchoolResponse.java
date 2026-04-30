package com.pathshalapro.dto.school;

import com.pathshalapro.entity.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolResponse {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    @JsonProperty("isActive")
    private boolean active;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime createdAt;
}
