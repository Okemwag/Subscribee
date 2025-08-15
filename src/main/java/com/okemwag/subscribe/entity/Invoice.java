package com.okemwag.subscribe.entity;

import com.okemwag.subscribe.enums.InvoiceStatus;
import lombok.Data;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Subscription is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Subtotal cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Subtotal must have at most 8 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Tax amount must have at most 8 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Total amount must have at most 8 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Invoice status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 1, fraction = 4, message = "Tax rate must have at most 1 integer digit and 4 decimal places")
    @Column(precision = 5, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Payment> payments;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Generate invoice number if not set
        if (invoiceNumber == null || invoiceNumber.isEmpty()) {
            generateInvoiceNumber();
        }
        
        // Calculate tax and total if not set
        if (taxAmount == null && subtotal != null && taxRate != null) {
            calculateTaxAndTotal();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Recalculate tax and total if subtotal or tax rate changed
        if (subtotal != null && taxRate != null) {
            calculateTaxAndTotal();
        }
    }

    /**
     * Generate unique invoice number
     */
    private void generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-HHMMSS-XXX (where XXX is random)
        String timestamp = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 14);
        int random = (int) (Math.random() * 999) + 1;
        this.invoiceNumber = String.format("INV-%s-%03d", timestamp, random);
    }

    /**
     * Calculate tax amount and total amount based on subtotal and tax rate
     */
    public void calculateTaxAndTotal() {
        if (subtotal != null && taxRate != null) {
            this.taxAmount = subtotal.multiply(taxRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.totalAmount = subtotal.add(taxAmount);
        }
    }

    /**
     * Check if invoice is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && 
               LocalDateTime.now().isAfter(dueDate) && 
               status != InvoiceStatus.PAID && 
               status != InvoiceStatus.CANCELLED;
    }

    /**
     * Mark invoice as paid
     */
    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
    }
}