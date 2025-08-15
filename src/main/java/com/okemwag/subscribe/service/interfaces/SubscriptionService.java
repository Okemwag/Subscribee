package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.CreateSubscriptionDTO;
import com.okemwag.subscribe.dto.SubscriptionDTO;
import com.okemwag.subscribe.dto.UpdateSubscriptionDTO;
import com.okemwag.subscribe.enums.SubscriptionStatus;
import java.util.List;

public interface SubscriptionService {
  SubscriptionDTO createSubscription(CreateSubscriptionDTO dto);

  SubscriptionDTO updateSubscription(Long subscriptionId, UpdateSubscriptionDTO dto);

  void cancelSubscription(Long subscriptionId, String cancellationReason);

  List<SubscriptionDTO> getCustomerSubscriptions(Long customerId);

  SubscriptionDTO renewSubscription(Long subscriptionId);

  List<SubscriptionDTO> getSubscriptionsByStatus(SubscriptionStatus status);
}
