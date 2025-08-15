package com.okemwag.subscribe.dto;

import com.okemwag.subscribe.enums.SubscriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateSubscriptionDTO {
    private LocalDateTime endDate;
    private SubscriptionStatus status;
    private LocalDateTime nextBillingDate;
}