package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.AnalyticsDTO;
import com.okemwag.subscribe.dto.CustomerGrowthDTO;
import com.okemwag.subscribe.dto.RevenueReportDTO;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.*;
import com.okemwag.subscribe.service.interfaces.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Cacheable(value = "businessAnalytics", key = "#businessId")
    public AnalyticsDTO getBusinessAnalytics(Long businessId) {
        log.info("Generating analytics for business ID: {}", businessId);
        
        // Validate business exists
        if (!businessRepository.findByIdAndActive(businessId).isPresent()) {
            throw new ResourceNotFoundException("Business not found with ID: " + businessId);
        }

        try {
            AnalyticsDTO analytics = new AnalyticsDTO();
            analytics.setRevenueMetrics(calculateRevenueMetrics(businessId));
            analytics.setCustomerMetrics(calculateCustomerMetrics(businessId));
            analytics.setSubscriptionMetrics(calculateSubscriptionMetrics(businessId));
            analytics.setChurnMetrics(calculateChurnMetrics(businessId));
            analytics.setGeneratedAt(LocalDateTime.now());
            
            log.info("Successfully generated analytics for business ID: {}", businessId);
            return analytics;
            
        } catch (Exception e) {
            log.error("Error generating analytics for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to generate business analytics", e);
        }
    }

    @Override
    @Cacheable(value = "revenueReport", key = "#businessId + '_' + #startDate + '_' + #endDate")
    public RevenueReportDTO getRevenueReport(Long businessId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating revenue report for business ID: {} from {} to {}", businessId, startDate, endDate);
        
        // Validate business exists
        if (!businessRepository.findByIdAndActive(businessId).isPresent()) {
            throw new ResourceNotFoundException("Business not found with ID: " + businessId);
        }

        try {
            RevenueReportDTO report = new RevenueReportDTO();
            report.setReportPeriodStart(startDate);
            report.setReportPeriodEnd(endDate);
            report.setGeneratedAt(LocalDateTime.now());
            
            // Calculate revenue metrics
            report.setTotalPaidAmount(paymentRepository.getRevenueByBusinessAndDateRange(businessId, startDate, endDate));
            report.setTotalOutstandingAmount(invoiceRepository.getTotalOutstandingAmountByBusiness(businessId));
            report.setTotalRefunds(paymentRepository.getTotalRefundsByBusiness(businessId));
            report.setTotalRevenue(report.getTotalPaidAmount() != null ? report.getTotalPaidAmount() : BigDecimal.ZERO);
            
            // Monthly breakdown
            report.setMonthlyBreakdown(getMonthlyRevenueBreakdown(businessId));
            
            // Revenue by currency
            report.setRevenueByCurrency(getRevenueByCurrency(businessId));
            
            // Revenue by payment method
            report.setRevenueByPaymentMethod(getRevenueByPaymentMethod(businessId));
            
            log.info("Successfully generated revenue report for business ID: {}", businessId);
            return report;
            
        } catch (Exception e) {
            log.error("Error generating revenue report for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to generate revenue report", e);
        }
    }

    @Override
    @Cacheable(value = "customerGrowth", key = "#businessId")
    public CustomerGrowthDTO getCustomerGrowthReport(Long businessId) {
        log.info("Generating customer growth report for business ID: {}", businessId);
        
        // Validate business exists
        if (!businessRepository.findByIdAndActive(businessId).isPresent()) {
            throw new ResourceNotFoundException("Business not found with ID: " + businessId);
        }

        try {
            CustomerGrowthDTO report = new CustomerGrowthDTO();
            report.setGeneratedAt(LocalDateTime.now());
            
            // Current metrics
            report.setTotalCustomers(customerRepository.countAllCustomersByBusiness(businessId));
            report.setActiveCustomers(customerRepository.countActiveCustomersByBusiness(businessId));
            
            // Monthly growth
            LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
            
            report.setNewCustomersThisMonth(customerRepository.countCustomersCreatedSinceByBusiness(businessId, thisMonthStart));
            report.setNewCustomersLastMonth(customerRepository.countCustomersCreatedSinceByBusiness(businessId, lastMonthStart) - 
                report.getNewCustomersThisMonth());
            
            // Calculate growth rate
            if (report.getNewCustomersLastMonth() > 0) {
                double growthRate = ((double) (report.getNewCustomersThisMonth() - report.getNewCustomersLastMonth()) / 
                    report.getNewCustomersLastMonth()) * 100;
                report.setGrowthRate(Math.round(growthRate * 100.0) / 100.0);
            } else {
                report.setGrowthRate(report.getNewCustomersThisMonth() > 0 ? 100.0 : 0.0);
            }
            
            // Monthly growth breakdown (last 12 months)
            report.setMonthlyGrowth(getMonthlyCustomerGrowth(businessId));
            
            log.info("Successfully generated customer growth report for business ID: {}", businessId);
            return report;
            
        } catch (Exception e) {
            log.error("Error generating customer growth report for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to generate customer growth report", e);
        }
    }

    @Override
    public Double calculateChurnRate(Long businessId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating churn rate for business ID: {} from {} to {}", businessId, startDate, endDate);
        
        try {
            // Get customers at start of period
            Long customersAtStart = customerRepository.countCustomersCreatedSinceByBusiness(businessId, 
                LocalDateTime.of(1970, 1, 1, 0, 0)) - 
                customerRepository.countCustomersCreatedSinceByBusiness(businessId, startDate);
            
            // Get churned customers (cancelled subscriptions) in period
            List<Object[]> cancelledSubscriptions = subscriptionRepository
                .findCancelledSubscriptionsByBusinessAndDateRange(businessId, startDate, endDate)
                .stream()
                .map(sub -> new Object[]{sub.getCustomer().getId()})
                .distinct()
                .collect(Collectors.toList());
            
            Long churnedCustomers = (long) cancelledSubscriptions.size();
            
            if (customersAtStart == 0) {
                return 0.0;
            }
            
            double churnRate = (churnedCustomers.doubleValue() / customersAtStart.doubleValue()) * 100;
            return Math.round(churnRate * 100.0) / 100.0;
            
        } catch (Exception e) {
            log.error("Error calculating churn rate for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to calculate churn rate", e);
        }
    }

    @Override
    @CacheEvict(value = {"businessAnalytics", "revenueReport", "customerGrowth"}, key = "#businessId")
    public void refreshAnalyticsCache(Long businessId) {
        log.info("Refreshing analytics cache for business ID: {}", businessId);
        // Cache will be refreshed on next access
    }

    /**
     * Calculate revenue metrics for business
     */
    private AnalyticsDTO.RevenueMetrics calculateRevenueMetrics(Long businessId) {
        AnalyticsDTO.RevenueMetrics metrics = new AnalyticsDTO.RevenueMetrics();
        
        // Total revenue
        BigDecimal totalRevenue = paymentRepository.getTotalRevenueByBusiness(businessId);
        metrics.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // Monthly recurring revenue (MRR) - approximate based on active subscriptions
        Long activeSubscriptions = subscriptionRepository.countActiveSubscriptionsByBusiness(businessId);
        if (activeSubscriptions > 0) {
            // This is a simplified calculation - in reality, you'd need to consider different billing cycles
            BigDecimal avgSubscriptionValue = metrics.getTotalRevenue().divide(
                BigDecimal.valueOf(activeSubscriptions), 2, RoundingMode.HALF_UP);
            metrics.setMonthlyRecurringRevenue(avgSubscriptionValue);
        } else {
            metrics.setMonthlyRecurringRevenue(BigDecimal.ZERO);
        }
        
        // Average revenue per user (ARPU)
        Long totalCustomers = customerRepository.countActiveCustomersByBusiness(businessId);
        if (totalCustomers > 0) {
            metrics.setAverageRevenuePerUser(metrics.getTotalRevenue().divide(
                BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP));
        } else {
            metrics.setAverageRevenuePerUser(BigDecimal.ZERO);
        }
        
        // Outstanding amount
        BigDecimal outstanding = invoiceRepository.getTotalOutstandingAmountByBusiness(businessId);
        metrics.setTotalOutstanding(outstanding != null ? outstanding : BigDecimal.ZERO);
        
        // Revenue by month
        metrics.setRevenueByMonth(getMonthlyRevenueMap(businessId));
        
        // Revenue by currency
        metrics.setRevenueByCurrency(getRevenueByCurrency(businessId));
        
        return metrics;
    }

    /**
     * Calculate customer metrics for business
     */
    private AnalyticsDTO.CustomerMetrics calculateCustomerMetrics(Long businessId) {
        AnalyticsDTO.CustomerMetrics metrics = new AnalyticsDTO.CustomerMetrics();
        
        metrics.setTotalCustomers(customerRepository.countAllCustomersByBusiness(businessId));
        metrics.setActiveCustomers(customerRepository.countActiveCustomersByBusiness(businessId));
        
        // New customers this month
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        metrics.setNewCustomersThisMonth(customerRepository.countCustomersCreatedSinceByBusiness(businessId, monthStart));
        
        // Customers without subscriptions
        // This would require a custom query - simplified for now
        metrics.setCustomersWithoutSubscriptions(0L);
        
        // Customer growth rate (month over month)
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        Long lastMonthNew = customerRepository.countCustomersCreatedSinceByBusiness(businessId, lastMonthStart) - 
            metrics.getNewCustomersThisMonth();
        
        if (lastMonthNew > 0) {
            double growthRate = ((double) (metrics.getNewCustomersThisMonth() - lastMonthNew) / lastMonthNew) * 100;
            metrics.setCustomerGrowthRate(Math.round(growthRate * 100.0) / 100.0);
        } else {
            metrics.setCustomerGrowthRate(metrics.getNewCustomersThisMonth() > 0 ? 100.0 : 0.0);
        }
        
        // Customers by language
        metrics.setCustomersByLanguage(getCustomersByLanguage(businessId));
        
        return metrics;
    }

    /**
     * Calculate subscription metrics for business
     */
    private AnalyticsDTO.SubscriptionMetrics calculateSubscriptionMetrics(Long businessId) {
        AnalyticsDTO.SubscriptionMetrics metrics = new AnalyticsDTO.SubscriptionMetrics();
        
        metrics.setTotalSubscriptions(subscriptionRepository.countAllSubscriptionsByBusiness(businessId));
        metrics.setActiveSubscriptions(subscriptionRepository.countActiveSubscriptionsByBusiness(businessId));
        metrics.setTrialSubscriptions(subscriptionRepository.countTrialSubscriptionsByBusiness(businessId));
        
        // Subscriptions by status
        Map<String, Long> subscriptionsByStatus = new HashMap<>();
        List<Object[]> statusCounts = subscriptionRepository.countSubscriptionsByStatusAndBusiness(businessId);
        for (Object[] row : statusCounts) {
            subscriptionsByStatus.put(row[0].toString(), (Long) row[1]);
        }
        metrics.setSubscriptionsByStatus(subscriptionsByStatus);
        
        // Cancelled subscriptions
        metrics.setCancelledSubscriptions(subscriptionsByStatus.getOrDefault("CANCELLED", 0L));
        
        // Subscriptions by plan
        Map<String, Long> subscriptionsByPlan = new HashMap<>();
        List<Object[]> planCounts = subscriptionRepository.countActiveSubscriptionsByPlanAndBusiness(businessId);
        for (Object[] row : planCounts) {
            subscriptionsByPlan.put((String) row[0], (Long) row[1]);
        }
        metrics.setSubscriptionsByPlan(subscriptionsByPlan);
        
        // Conversion rate (trial to active)
        if (metrics.getTrialSubscriptions() > 0) {
            double conversionRate = (metrics.getActiveSubscriptions().doubleValue() / 
                (metrics.getActiveSubscriptions() + metrics.getTrialSubscriptions()).doubleValue()) * 100;
            metrics.setConversionRate(Math.round(conversionRate * 100.0) / 100.0);
        } else {
            metrics.setConversionRate(100.0);
        }
        
        return metrics;
    }

    /**
     * Calculate churn metrics for business
     */
    private AnalyticsDTO.ChurnMetrics calculateChurnMetrics(Long businessId) {
        AnalyticsDTO.ChurnMetrics metrics = new AnalyticsDTO.ChurnMetrics();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        // Monthly churn rate
        metrics.setMonthlyChurnRate(calculateChurnRate(businessId, monthStart, now));
        
        // Annual churn rate
        metrics.setAnnualChurnRate(calculateChurnRate(businessId, yearStart, now));
        
        // Churned customers this month
        List<Object[]> churnedThisMonth = subscriptionRepository
            .findCancelledSubscriptionsByBusinessAndDateRange(businessId, monthStart, now)
            .stream()
            .map(sub -> new Object[]{sub.getCustomer().getId()})
            .distinct()
            .collect(Collectors.toList());
        metrics.setChurnedCustomersThisMonth((long) churnedThisMonth.size());
        
        // Churned revenue this month (simplified calculation)
        metrics.setChurnedRevenueThisMonth(BigDecimal.ZERO); // Would need more complex calculation
        
        // Customer lifetime value (simplified)
        BigDecimal totalRevenue = paymentRepository.getTotalRevenueByBusiness(businessId);
        Long totalCustomers = customerRepository.countAllCustomersByBusiness(businessId);
        if (totalCustomers > 0 && totalRevenue != null) {
            double clv = totalRevenue.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP).doubleValue();
            metrics.setCustomerLifetimeValue(Math.round(clv * 100.0) / 100.0);
        } else {
            metrics.setCustomerLifetimeValue(0.0);
        }
        
        return metrics;
    }

    /**
     * Get monthly revenue breakdown
     */
    private List<RevenueReportDTO.MonthlyRevenue> getMonthlyRevenueBreakdown(Long businessId) {
        List<Object[]> monthlyData = paymentRepository.getMonthlyRevenueByBusiness(businessId);
        
        return monthlyData.stream().map(row -> {
            RevenueReportDTO.MonthlyRevenue monthly = new RevenueReportDTO.MonthlyRevenue();
            monthly.setYear((Integer) row[0]);
            monthly.setMonth((Integer) row[1]);
            monthly.setRevenue((BigDecimal) row[2]);
            // Additional calculations would be needed for invoice count and average
            monthly.setInvoiceCount(0L);
            monthly.setAverageInvoiceAmount(BigDecimal.ZERO);
            return monthly;
        }).collect(Collectors.toList());
    }

    /**
     * Get monthly revenue as map
     */
    private Map<String, BigDecimal> getMonthlyRevenueMap(Long businessId) {
        List<Object[]> monthlyData = paymentRepository.getMonthlyRevenueByBusiness(businessId);
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        
        for (Object[] row : monthlyData) {
            String monthKey = row[0] + "-" + String.format("%02d", (Integer) row[1]);
            revenueMap.put(monthKey, (BigDecimal) row[2]);
        }
        
        return revenueMap;
    }

    /**
     * Get revenue by currency
     */
    private Map<String, BigDecimal> getRevenueByCurrency(Long businessId) {
        List<Object[]> currencyData = paymentRepository.getRevenueByCurrencyAndBusiness(businessId);
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        
        for (Object[] row : currencyData) {
            revenueMap.put((String) row[0], (BigDecimal) row[1]);
        }
        
        return revenueMap;
    }

    /**
     * Get revenue by payment method
     */
    private Map<String, BigDecimal> getRevenueByPaymentMethod(Long businessId) {
        // This would require a custom query - simplified for now
        Map<String, BigDecimal> methodMap = new HashMap<>();
        methodMap.put("STRIPE_CARD", BigDecimal.ZERO);
        methodMap.put("MPESA", BigDecimal.ZERO);
        methodMap.put("BANK_TRANSFER", BigDecimal.ZERO);
        return methodMap;
    }

    /**
     * Get customers by language
     */
    private Map<String, Long> getCustomersByLanguage(Long businessId) {
        List<Object[]> languageData = customerRepository.countCustomersByLanguageAndBusiness(businessId);
        Map<String, Long> languageMap = new HashMap<>();
        
        for (Object[] row : languageData) {
            languageMap.put((String) row[0], (Long) row[1]);
        }
        
        return languageMap;
    }

    /**
     * Get monthly customer growth
     */
    private List<CustomerGrowthDTO.MonthlyGrowth> getMonthlyCustomerGrowth(Long businessId) {
        // This would require more complex queries - simplified for now
        return List.of();
    }
}