package com.okemwag.subscribe.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerGrowthDTO {
    private Long totalCustomers;
    private Long activeCustomers;
    private Long newCustomersThisMonth;
    private Long newCustomersLastMonth;
    private Double growthRate;
    private List<MonthlyGrowth> monthlyGrowth;
    private LocalDateTime generatedAt;

    @Data
    public static class MonthlyGrowth {
        private int year;
        private int month;
        private Long newCustomers;
        private Long totalCustomers;
        private Double growthRate;
    }
}