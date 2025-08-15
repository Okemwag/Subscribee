package com.okemwag.subscribe.dto;

import com.okemwag.subscribe.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InvoiceDTO {
  private Long id;
  private Long subscriptionId;
  private String invoiceNumber;
  private BigDecimal subtotal;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private InvoiceStatus status;
  private BigDecimal taxRate;
  private LocalDateTime dueDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean overdue;
}
