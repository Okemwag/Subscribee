package com.okemwag.subscribe.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateSubscriptionDTO {
  @NotNull(message = "Customer ID is required")
  private Long customerId;

  @NotNull(message = "Subscription Plan ID is required")
  private Long subscriptionPlanId;

  private LocalDateTime startDate = LocalDateTime.now();
}
