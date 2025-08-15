package com.okemwag.subscribe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class RevenueReportDTO {
  private BigDecimal totalRevenue;
  private BigDecimal totalPaidAmount;
  private BigDecimal totalOutstandingAmount;
  private BigDecimal totalRefunds;
  private LocalDateTime reportPeriodStart;
  private LocalDateTime reportPeriodEnd;
  private List<MonthlyRevenue> monthlyBreakdown;
  private Map<String, BigDecimal> revenueByCurrency;
  private Map<String, BigDecimal> revenueByPaymentMethod;
  private LocalDateTime generatedAt;

  @Data
  public static class MonthlyRevenue {
    private int year;
    private int month;
    private BigDecimal revenue;
    private Long invoiceCount;
    private BigDecimal averageInvoiceAmount;
  }
}
