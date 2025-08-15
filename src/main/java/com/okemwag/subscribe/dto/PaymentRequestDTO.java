package com.okemwag.subscribe.dto;

import com.okemwag.subscribe.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentRequestDTO {
  @NotNull(message = "Subscription ID is required")
  private Long subscriptionId;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
  @Digits(
      integer = 8,
      fraction = 2,
      message = "Amount must have at most 8 integer digits and 2 decimal places")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
  private String currency;

  @NotNull(message = "Payment method is required")
  private PaymentMethod method;

  // Payment method specific fields
  private String cardToken; // For Stripe payments
  private String phoneNumber; // For M-Pesa payments
  private String bankAccountNumber; // For bank transfers
  private String routingNumber; // For bank transfers
}
