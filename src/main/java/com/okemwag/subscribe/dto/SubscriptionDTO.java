package com.okemwag.subscribe.dto;

import com.okemwag.subscribe.enums.SubscriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    private Long id;
    private Long customerId;
    private Long subscriptionPlanId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubscriptionStatus status;
    private LocalDateTime nextBillingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}