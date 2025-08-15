package com.okemwag.subscribe.entity;

import com.okemwag.subscribe.enums.PaymentMethod;
import com.okemwag.subscribe.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
@Table(name = "payments")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Subscription is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private Subscription subscription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id")
  private Invoice invoice;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
  @Digits(
      integer = 8,
      fraction = 2,
      message = "Amount must have at most 8 integer digits and 2 decimal places")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
  @Column(nullable = false, length = 3)
  private String currency;

  @NotNull(message = "Payment status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status = PaymentStatus.PENDING;

  @NotNull(message = "Payment method is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentMethod method;

  @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
  @Column(length = 255)
  private String transactionId;

  private LocalDateTime processedAt;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
