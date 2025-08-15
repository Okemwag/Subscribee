package com.okemwag.subscribe.dto;

import com.okemwag.subscribe.enums.SubscriptionStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UpdateSubscriptionDTO {
  private LocalDateTime endDate;
  private SubscriptionStatus status;
  private LocalDateTime nextBillingDate;
}
