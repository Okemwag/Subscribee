package com.okemwag.subscribe.dto;

import com.okemwag.subscribe.enums.PaymentMethod;
import com.okemwag.subscribe.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentDTO {
  private Long id;
  private Long subscriptionId;
  private Long invoiceId;
  private BigDecimal amount;
  private String currency;
  private PaymentStatus status;
  private PaymentMethod method;
  private String transactionId;
  private LocalDateTime processedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
