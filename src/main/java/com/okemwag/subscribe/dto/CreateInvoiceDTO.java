package com.okemwag.subscribe.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateInvoiceDTO {
  @NotNull(message = "Subscription ID is required")
  private Long subscriptionId;

  @NotNull(message = "Subtotal is required")
  @DecimalMin(value = "0.0", message = "Subtotal cannot be negative")
  @Digits(
      integer = 8,
      fraction = 2,
      message = "Subtotal must have at most 8 integer digits and 2 decimal places")
  private BigDecimal subtotal;

  @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
  @DecimalMax(value = "1.0", message = "Tax rate cannot exceed 100%")
  @Digits(
      integer = 1,
      fraction = 4,
      message = "Tax rate must have at most 1 integer digit and 4 decimal places")
  private BigDecimal taxRate = BigDecimal.ZERO;

  @Future(message = "Due date must be in the future")
  private LocalDateTime dueDate;
}
