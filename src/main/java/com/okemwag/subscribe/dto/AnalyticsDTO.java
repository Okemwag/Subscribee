package com.okemwag.subscribe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class AnalyticsDTO {
  private RevenueMetrics revenueMetrics;
  private CustomerMetrics customerMetrics;
  private SubscriptionMetrics subscriptionMetrics;
  private ChurnMetrics churnMetrics;
  private LocalDateTime generatedAt;

  @Data
  public static class RevenueMetrics {
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRecurringRevenue;
    private BigDecimal averageRevenuePerUser;
    private BigDecimal totalOutstanding;
    private Map<String, BigDecimal> revenueByMonth;
    private Map<String, BigDecimal> revenueByCurrency;
  }

  @Data
  public static class CustomerMetrics {
    private Long totalCustomers;
    private Long activeCustomers;
    private Long newCustomersThisMonth;
    private Long customersWithoutSubscriptions;
    private Double customerGrowthRate;
    private Map<String, Long> customersByLanguage;
  }

  @Data
  public static class SubscriptionMetrics {
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long trialSubscriptions;
    private Long cancelledSubscriptions;
    private Map<String, Long> subscriptionsByStatus;
    private Map<String, Long> subscriptionsByPlan;
    private Double conversionRate;
  }

  @Data
  public static class ChurnMetrics {
    private Double monthlyChurnRate;
    private Double annualChurnRate;
    private Long churnedCustomersThisMonth;
    private BigDecimal churnedRevenueThisMonth;
    private Double customerLifetimeValue;
  }
}
