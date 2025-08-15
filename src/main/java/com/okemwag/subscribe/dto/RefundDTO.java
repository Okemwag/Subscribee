package com.okemwag.subscribe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RefundDTO {
  private Long id;
  private Long originalPaymentId;
  private BigDecimal amount;
  private String currency;
  private String reason;
  private String refundTransactionId;
  private LocalDateTime processedAt;
  private LocalDateTime createdAt;
}
