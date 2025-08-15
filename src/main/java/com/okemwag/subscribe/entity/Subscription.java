package com.okemwag.subscribe.entity;

import com.okemwag.subscribe.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Entity
@Data
@Table(name = "subscriptions")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Customer is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @NotNull(message = "Subscription plan is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_plan_id", nullable = false)
  private SubscriptionPlan subscriptionPlan;

  @NotNull(message = "Start date is required")
  @PastOrPresent(message = "Start date cannot be in the future")
  @Column(nullable = false)
  private LocalDateTime startDate;

  @Future(message = "End date must be in the future")
  private LocalDateTime endDate;

  @NotNull(message = "Status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  @Future(message = "Next billing date must be in the future")
  private LocalDateTime nextBillingDate;

  @OneToMany(
      mappedBy = "subscription",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private Set<Payment> payments;

  @OneToMany(
      mappedBy = "subscription",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private Set<Invoice> invoices;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();

    // Set initial next billing date if not set
    if (nextBillingDate == null && startDate != null && subscriptionPlan != null) {
      calculateNextBillingDate();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /** Calculate next billing date based on billing cycle */
  private void calculateNextBillingDate() {
    if (startDate != null
        && subscriptionPlan != null
        && subscriptionPlan.getBillingCycle() != null) {
      switch (subscriptionPlan.getBillingCycle()) {
        case MONTHLY:
          nextBillingDate = startDate.plusMonths(1);
          break;
        case QUARTERLY:
          nextBillingDate = startDate.plusMonths(3);
          break;
        case YEARLY:
          nextBillingDate = startDate.plusYears(1);
          break;
      }
    }
  }

  /** Transition subscription status with validation */
  public void transitionStatus(SubscriptionStatus newStatus) {
    if (canTransitionTo(newStatus)) {
      this.status = newStatus;

      // Update end date when cancelled or expired
      if (newStatus == SubscriptionStatus.CANCELLED || newStatus == SubscriptionStatus.EXPIRED) {
        this.endDate = LocalDateTime.now();
        this.nextBillingDate = null;
      }
    } else {
      throw new IllegalStateException("Cannot transition from " + this.status + " to " + newStatus);
    }
  }

  /** Check if status transition is valid */
  private boolean canTransitionTo(SubscriptionStatus newStatus) {
    switch (this.status) {
      case TRIAL:
        return newStatus == SubscriptionStatus.ACTIVE
            || newStatus == SubscriptionStatus.CANCELLED
            || newStatus == SubscriptionStatus.EXPIRED;
      case ACTIVE:
        return newStatus == SubscriptionStatus.CANCELLED
            || newStatus == SubscriptionStatus.SUSPENDED
            || newStatus == SubscriptionStatus.EXPIRED;
      case SUSPENDED:
        return newStatus == SubscriptionStatus.ACTIVE
            || newStatus == SubscriptionStatus.CANCELLED
            || newStatus == SubscriptionStatus.EXPIRED;
      case CANCELLED:
      case EXPIRED:
        return false; // Terminal states
      default:
        return false;
    }
  }
}
