package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.AnalyticsDTO;
import com.okemwag.subscribe.dto.CustomerGrowthDTO;
import com.okemwag.subscribe.dto.RevenueReportDTO;

import java.time.LocalDateTime;

public interface AnalyticsService {
    AnalyticsDTO getBusinessAnalytics(Long businessId);
    RevenueReportDTO getRevenueReport(Long businessId, LocalDateTime startDate, LocalDateTime endDate);
    CustomerGrowthDTO getCustomerGrowthReport(Long businessId);
    Double calculateChurnRate(Long businessId, LocalDateTime startDate, LocalDateTime endDate);
    void refreshAnalyticsCache(Long businessId);
}