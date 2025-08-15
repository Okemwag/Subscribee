package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.CreateSubscriptionDTO;
import com.okemwag.subscribe.dto.SubscriptionDTO;
import com.okemwag.subscribe.dto.UpdateSubscriptionDTO;
import com.okemwag.subscribe.entity.Customer;
import com.okemwag.subscribe.entity.Subscription;
import com.okemwag.subscribe.entity.SubscriptionPlan;
import com.okemwag.subscribe.enums.BillingCycle;
import com.okemwag.subscribe.enums.SubscriptionStatus;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.CustomerRepository;
import com.okemwag.subscribe.repository.SubscriptionPlanRepository;
import com.okemwag.subscribe.repository.SubscriptionRepository;
import com.okemwag.subscribe.service.interfaces.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public SubscriptionDTO createSubscription(CreateSubscriptionDTO dto) {
        log.info("Creating subscription for customer ID: {} with plan ID: {}", 
            dto.getCustomerId(), dto.getSubscriptionPlanId());
        
        // Validate customer exists and is active
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + dto.getCustomerId()));
        
        if (!customer.getActive()) {
            throw new SubscribeException("Cannot create subscription for inactive customer");
        }

        // Validate subscription plan exists and is active within the same business
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository
            .findByIdAndBusinessIdAndActive(dto.getSubscriptionPlanId(), customer.getBusiness().getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Subscription plan not found with ID: " + dto.getSubscriptionPlanId() + 
                " for business: " + customer.getBusiness().getId()));

        // Check if customer already has an active subscription to this plan
        List<Subscription> existingActiveSubscriptions = subscriptionRepository
            .findByCustomerIdAndStatus(dto.getCustomerId(), SubscriptionStatus.ACTIVE);
        
        boolean hasActivePlanSubscription = existingActiveSubscriptions.stream()
            .anyMatch(sub -> sub.getSubscriptionPlan().getId().equals(dto.getSubscriptionPlanId()));
        
        if (hasActivePlanSubscription) {
            throw new SubscribeException("Customer already has an active subscription to this plan");
        }

        // Create new subscription
        Subscription subscription = new Subscription();
        subscription.setCustomer(customer);
        subscription.setSubscriptionPlan(subscriptionPlan);
        subscription.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDateTime.now());
        
        // Set status based on trial period
        if (subscriptionPlan.getTrialDays() > 0) {
            subscription.setStatus(SubscriptionStatus.TRIAL);
            subscription.setEndDate(subscription.getStartDate().plusDays(subscriptionPlan.getTrialDays()));
        } else {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        
        // Calculate next billing date
        subscription.setNextBillingDate(calculateNextBillingDate(subscription.getStartDate(), 
            subscriptionPlan.getBillingCycle()));

        try {
            Subscription savedSubscription = subscriptionRepository.save(subscription);
            log.info("Successfully created subscription with ID: {} for customer ID: {}", 
                savedSubscription.getId(), dto.getCustomerId());
            return convertToDTO(savedSubscription);
        } catch (Exception e) {
            log.error("Error creating subscription: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to create subscription", e);
        }
    }

    @Override
    public SubscriptionDTO updateSubscription(Long subscriptionId, UpdateSubscriptionDTO dto) {
        log.info("Updating subscription with ID: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        // Update fields if provided
        if (dto.getEndDate() != null) {
            subscription.setEndDate(dto.getEndDate());
        }
        
        if (dto.getStatus() != null) {
            subscription.transitionStatus(dto.getStatus());
        }
        
        if (dto.getNextBillingDate() != null) {
            subscription.setNextBillingDate(dto.getNextBillingDate());
        }

        try {
            Subscription updatedSubscription = subscriptionRepository.save(subscription);
            log.info("Successfully updated subscription with ID: {}", subscriptionId);
            return convertToDTO(updatedSubscription);
        } catch (Exception e) {
            log.error("Error updating subscription with ID {}: {}", subscriptionId, e.getMessage(), e);
            throw new SubscribeException("Failed to update subscription", e);
        }
    }

    @Override
    public void cancelSubscription(Long subscriptionId, String cancellationReason) {
        log.info("Cancelling subscription with ID: {} with reason: {}", subscriptionId, cancellationReason);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        // Validate subscription can be cancelled
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED || 
            subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new SubscribeException("Subscription is already " + subscription.getStatus().toString().toLowerCase());
        }

        try {
            subscription.transitionStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
            log.info("Successfully cancelled subscription with ID: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Error cancelling subscription with ID {}: {}", subscriptionId, e.getMessage(), e);
            throw new SubscribeException("Failed to cancel subscription", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getCustomerSubscriptions(Long customerId) {
        log.debug("Retrieving subscriptions for customer ID: {}", customerId);
        
        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        try {
            return subscriptionRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving subscriptions for customer ID {}: {}", customerId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve customer subscriptions", e);
        }
    }

    @Override
    public SubscriptionDTO renewSubscription(Long subscriptionId) {
        log.info("Renewing subscription with ID: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        // Validate subscription can be renewed
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && 
            subscription.getStatus() != SubscriptionStatus.TRIAL) {
            throw new SubscribeException("Cannot renew subscription with status: " + subscription.getStatus());
        }

        try {
            // Update next billing date based on billing cycle
            LocalDateTime currentBillingDate = subscription.getNextBillingDate() != null ? 
                subscription.getNextBillingDate() : LocalDateTime.now();
            
            subscription.setNextBillingDate(calculateNextBillingDate(currentBillingDate, 
                subscription.getSubscriptionPlan().getBillingCycle()));
            
            // If it was a trial, convert to active
            if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
                subscription.transitionStatus(SubscriptionStatus.ACTIVE);
            }

            Subscription renewedSubscription = subscriptionRepository.save(subscription);
            log.info("Successfully renewed subscription with ID: {}", subscriptionId);
            return convertToDTO(renewedSubscription);
        } catch (Exception e) {
            log.error("Error renewing subscription with ID {}: {}", subscriptionId, e.getMessage(), e);
            throw new SubscribeException("Failed to renew subscription", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getSubscriptionsByStatus(SubscriptionStatus status) {
        log.debug("Retrieving subscriptions with status: {}", status);
        
        try {
            return subscriptionRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving subscriptions with status {}: {}", status, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve subscriptions by status", e);
        }
    }

    /**
     * Gets subscriptions by business for multi-tenant operations
     */
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getSubscriptionsByBusiness(Long businessId) {
        log.debug("Retrieving subscriptions for business ID: {}", businessId);
        
        try {
            return subscriptionRepository.findByCustomerBusinessId(businessId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving subscriptions for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve business subscriptions", e);
        }
    }

    /**
     * Gets subscriptions due for billing
     */
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getSubscriptionsDueForBilling(LocalDateTime date) {
        log.debug("Retrieving subscriptions due for billing by date: {}", date);
        
        try {
            return subscriptionRepository.findSubscriptionsDueForBilling(date)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving subscriptions due for billing: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve subscriptions due for billing", e);
        }
    }

    /**
     * Processes expired subscriptions
     */
    public void processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");
        
        try {
            List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredSubscriptions(LocalDateTime.now());
            
            for (Subscription subscription : expiredSubscriptions) {
                subscription.transitionStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
                log.info("Marked subscription {} as expired", subscription.getId());
            }
            
            log.info("Processed {} expired subscriptions", expiredSubscriptions.size());
        } catch (Exception e) {
            log.error("Error processing expired subscriptions: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to process expired subscriptions", e);
        }
    }

    /**
     * Validates subscription access for multi-tenant operations
     */
    public void validateSubscriptionBusinessAccess(Long subscriptionId, Long businessId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));
        
        if (!subscription.getCustomer().getBusiness().getId().equals(businessId)) {
            log.warn("Unauthorized access attempt: Business {} tried to access subscription {} from business {}", 
                businessId, subscriptionId, subscription.getCustomer().getBusiness().getId());
            throw new SubscribeException("Access denied: Subscription belongs to a different business");
        }
    }

    /**
     * Calculates next billing date based on billing cycle
     */
    private LocalDateTime calculateNextBillingDate(LocalDateTime currentDate, BillingCycle billingCycle) {
        switch (billingCycle) {
            case MONTHLY:
                return currentDate.plusMonths(1);
            case QUARTERLY:
                return currentDate.plusMonths(3);
            case YEARLY:
                return currentDate.plusYears(1);
            default:
                throw new IllegalArgumentException("Unsupported billing cycle: " + billingCycle);
        }
    }

    /**
     * Converts Subscription entity to SubscriptionDTO
     */
    private SubscriptionDTO convertToDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setCustomerId(subscription.getCustomer().getId());
        dto.setSubscriptionPlanId(subscription.getSubscriptionPlan().getId());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());
        dto.setStatus(subscription.getStatus());
        dto.setNextBillingDate(subscription.getNextBillingDate());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setUpdatedAt(subscription.getUpdatedAt());
        return dto;
    }
}