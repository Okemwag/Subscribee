package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.PaymentDTO;
import com.okemwag.subscribe.dto.PaymentRequestDTO;
import com.okemwag.subscribe.dto.RefundDTO;
import com.okemwag.subscribe.dto.RefundRequestDTO;
import com.okemwag.subscribe.entity.Payment;
import com.okemwag.subscribe.entity.Subscription;
import com.okemwag.subscribe.enums.PaymentStatus;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.PaymentRepository;
import com.okemwag.subscribe.repository.SubscriptionRepository;
import com.okemwag.subscribe.service.interfaces.PaymentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  public PaymentDTO processPayment(PaymentRequestDTO dto) {
    log.info(
        "Processing payment for subscription ID: {} with amount: {} {}",
        dto.getSubscriptionId(),
        dto.getAmount(),
        dto.getCurrency());

    // Validate subscription exists and is active
    Subscription subscription =
        subscriptionRepository
            .findById(dto.getSubscriptionId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Subscription not found with ID: " + dto.getSubscriptionId()));

    if (subscription.getStatus() != com.okemwag.subscribe.enums.SubscriptionStatus.ACTIVE
        && subscription.getStatus() != com.okemwag.subscribe.enums.SubscriptionStatus.TRIAL) {
      throw new SubscribeException("Cannot process payment for inactive subscription");
    }

    // Create payment record
    Payment payment = new Payment();
    payment.setSubscription(subscription);
    payment.setAmount(dto.getAmount());
    payment.setCurrency(dto.getCurrency());
    payment.setMethod(dto.getMethod());
    payment.setStatus(PaymentStatus.PENDING);

    try {
      // Save payment record first
      Payment savedPayment = paymentRepository.save(payment);

      // Process payment through gateway
      PaymentResult result = processPaymentThroughGateway(dto, savedPayment);

      // Update payment with result
      savedPayment.setStatus(result.getStatus());
      savedPayment.setTransactionId(result.getTransactionId());

      if (result.getStatus() == PaymentStatus.COMPLETED) {
        savedPayment.setProcessedAt(LocalDateTime.now());
        log.info(
            "Payment processed successfully with ID: {} and transaction ID: {}",
            savedPayment.getId(),
            result.getTransactionId());
      } else {
        log.warn(
            "Payment failed for ID: {} with reason: {}",
            savedPayment.getId(),
            result.getFailureReason());
      }

      Payment finalPayment = paymentRepository.save(savedPayment);
      return convertToDTO(finalPayment);

    } catch (Exception e) {
      log.error("Error processing payment: {}", e.getMessage(), e);

      // Update payment status to failed if it was created
      if (payment.getId() != null) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
      }

      throw new SubscribeException("Failed to process payment", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentDTO getPaymentById(Long paymentId) {
    log.debug("Retrieving payment with ID: {}", paymentId);

    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

    return convertToDTO(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDTO> getPaymentHistory(Long customerId, Pageable pageable) {
    log.debug("Retrieving payment history for customer ID: {}", customerId);

    try {
      return paymentRepository
          .findPaymentHistoryByCustomer(customerId, pageable)
          .getContent()
          .stream()
          .map(this::convertToDTO)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error(
          "Error retrieving payment history for customer ID {}: {}", customerId, e.getMessage(), e);
      throw new SubscribeException("Failed to retrieve payment history", e);
    }
  }

  @Override
  public RefundDTO processRefund(Long paymentId, RefundRequestDTO dto) {
    log.info("Processing refund for payment ID: {} with amount: {}", paymentId, dto.getAmount());

    Payment originalPayment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

    // Validate payment can be refunded
    if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
      throw new SubscribeException("Cannot refund payment that is not completed");
    }

    if (dto.getAmount().compareTo(originalPayment.getAmount()) > 0) {
      throw new SubscribeException("Refund amount cannot exceed original payment amount");
    }

    try {
      // Process refund through gateway
      RefundResult result = processRefundThroughGateway(originalPayment, dto);

      if (result.isSuccessful()) {
        // Update original payment status
        originalPayment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(originalPayment);

        log.info(
            "Refund processed successfully for payment ID: {} with refund transaction ID: {}",
            paymentId,
            result.getRefundTransactionId());

        // Create and return refund DTO
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setOriginalPaymentId(paymentId);
        refundDTO.setAmount(dto.getAmount());
        refundDTO.setCurrency(originalPayment.getCurrency());
        refundDTO.setReason(dto.getReason());
        refundDTO.setRefundTransactionId(result.getRefundTransactionId());
        refundDTO.setProcessedAt(LocalDateTime.now());
        refundDTO.setCreatedAt(LocalDateTime.now());

        return refundDTO;
      } else {
        throw new SubscribeException("Refund failed: " + result.getFailureReason());
      }

    } catch (Exception e) {
      log.error("Error processing refund for payment ID {}: {}", paymentId, e.getMessage(), e);
      throw new SubscribeException("Failed to process refund", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDTO> getPaymentsBySubscription(Long subscriptionId) {
    log.debug("Retrieving payments for subscription ID: {}", subscriptionId);

    // Validate subscription exists
    if (!subscriptionRepository.existsById(subscriptionId)) {
      throw new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId);
    }

    try {
      return paymentRepository.findPaymentHistoryBySubscription(subscriptionId).stream()
          .map(this::convertToDTO)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error(
          "Error retrieving payments for subscription ID {}: {}",
          subscriptionId,
          e.getMessage(),
          e);
      throw new SubscribeException("Failed to retrieve subscription payments", e);
    }
  }

  @Override
  public void processFailedPaymentRetries() {
    log.info("Processing failed payment retries");

    try {
      // Find payments that failed more than 1 hour ago for retry
      LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
      List<Payment> failedPayments =
          paymentRepository.findStalePaymentsByStatus(PaymentStatus.FAILED, cutoffTime);

      for (Payment payment : failedPayments) {
        try {
          // Attempt to retry the payment
          PaymentRequestDTO retryRequest = createRetryRequest(payment);
          PaymentResult result = processPaymentThroughGateway(retryRequest, payment);

          payment.setStatus(result.getStatus());
          payment.setTransactionId(result.getTransactionId());

          if (result.getStatus() == PaymentStatus.COMPLETED) {
            payment.setProcessedAt(LocalDateTime.now());
            log.info("Retry successful for payment ID: {}", payment.getId());
          } else {
            log.warn("Retry failed for payment ID: {}", payment.getId());
          }

          paymentRepository.save(payment);

        } catch (Exception e) {
          log.error("Error retrying payment ID {}: {}", payment.getId(), e.getMessage(), e);
          // Continue with other payments
        }
      }

      log.info("Processed {} failed payment retries", failedPayments.size());

    } catch (Exception e) {
      log.error("Error processing failed payment retries: {}", e.getMessage(), e);
      throw new SubscribeException("Failed to process payment retries", e);
    }
  }

  /** Gets payments by business for multi-tenant operations */
  @Transactional(readOnly = true)
  public List<PaymentDTO> getPaymentsByBusiness(Long businessId, Pageable pageable) {
    log.debug("Retrieving payments for business ID: {}", businessId);

    try {
      return paymentRepository
          .findByBusinessIdOrderByCreatedAt(businessId, pageable)
          .getContent()
          .stream()
          .map(this::convertToDTO)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error retrieving payments for business ID {}: {}", businessId, e.getMessage(), e);
      throw new SubscribeException("Failed to retrieve business payments", e);
    }
  }

  /** Validates payment access for multi-tenant operations */
  public void validatePaymentBusinessAccess(Long paymentId, Long businessId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

    if (!payment.getSubscription().getCustomer().getBusiness().getId().equals(businessId)) {
      log.warn(
          "Unauthorized access attempt: Business {} tried to access payment {} from business {}",
          businessId,
          paymentId,
          payment.getSubscription().getCustomer().getBusiness().getId());
      throw new SubscribeException("Access denied: Payment belongs to a different business");
    }
  }

  /** Processes payment through appropriate gateway based on method */
  private PaymentResult processPaymentThroughGateway(PaymentRequestDTO dto, Payment payment) {
    switch (dto.getMethod()) {
      case STRIPE_CARD:
        return processStripePayment(dto, payment);
      case MPESA:
        return processMpesaPayment(dto, payment);
      case BANK_TRANSFER:
        return processBankTransferPayment(dto, payment);
      default:
        throw new SubscribeException("Unsupported payment method: " + dto.getMethod());
    }
  }

  /** Processes Stripe card payment */
  private PaymentResult processStripePayment(PaymentRequestDTO dto, Payment payment) {
    // Mock implementation - in real scenario, integrate with Stripe API
    log.info("Processing Stripe payment for amount: {} {}", dto.getAmount(), dto.getCurrency());

    try {
      // Simulate payment processing
      Thread.sleep(1000); // Simulate network delay

      // Mock success/failure based on amount (for testing)
      boolean success = dto.getAmount().doubleValue() < 10000; // Fail for amounts >= 10000

      PaymentResult result = new PaymentResult();
      if (success) {
        result.setStatus(PaymentStatus.COMPLETED);
        result.setTransactionId("stripe_" + UUID.randomUUID().toString());
      } else {
        result.setStatus(PaymentStatus.FAILED);
        result.setFailureReason("Card declined");
      }

      return result;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SubscribeException("Payment processing interrupted", e);
    }
  }

  /** Processes M-Pesa payment */
  private PaymentResult processMpesaPayment(PaymentRequestDTO dto, Payment payment) {
    // Mock implementation - in real scenario, integrate with M-Pesa Daraja API
    log.info("Processing M-Pesa payment for amount: {} {}", dto.getAmount(), dto.getCurrency());

    try {
      // Simulate payment processing
      Thread.sleep(2000); // Simulate network delay

      PaymentResult result = new PaymentResult();
      result.setStatus(PaymentStatus.COMPLETED);
      result.setTransactionId("mpesa_" + UUID.randomUUID().toString());

      return result;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SubscribeException("Payment processing interrupted", e);
    }
  }

  /** Processes bank transfer payment */
  private PaymentResult processBankTransferPayment(PaymentRequestDTO dto, Payment payment) {
    // Mock implementation - in real scenario, integrate with banking API
    log.info(
        "Processing bank transfer payment for amount: {} {}", dto.getAmount(), dto.getCurrency());

    PaymentResult result = new PaymentResult();
    result.setStatus(PaymentStatus.PENDING); // Bank transfers typically require manual verification
    result.setTransactionId("bank_" + UUID.randomUUID().toString());

    return result;
  }

  /** Processes refund through appropriate gateway */
  private RefundResult processRefundThroughGateway(Payment originalPayment, RefundRequestDTO dto) {
    // Mock implementation - in real scenario, integrate with payment gateway APIs
    log.info("Processing refund through {} gateway", originalPayment.getMethod());

    try {
      Thread.sleep(1000); // Simulate network delay

      RefundResult result = new RefundResult();
      result.setSuccessful(true);
      result.setRefundTransactionId("refund_" + UUID.randomUUID().toString());

      return result;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SubscribeException("Refund processing interrupted", e);
    }
  }

  /** Creates retry request from failed payment */
  private PaymentRequestDTO createRetryRequest(Payment payment) {
    PaymentRequestDTO dto = new PaymentRequestDTO();
    dto.setSubscriptionId(payment.getSubscription().getId());
    dto.setAmount(payment.getAmount());
    dto.setCurrency(payment.getCurrency());
    dto.setMethod(payment.getMethod());
    return dto;
  }

  /** Converts Payment entity to PaymentDTO */
  private PaymentDTO convertToDTO(Payment payment) {
    PaymentDTO dto = new PaymentDTO();
    dto.setId(payment.getId());
    dto.setSubscriptionId(payment.getSubscription().getId());
    dto.setInvoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null);
    dto.setAmount(payment.getAmount());
    dto.setCurrency(payment.getCurrency());
    dto.setStatus(payment.getStatus());
    dto.setMethod(payment.getMethod());
    dto.setTransactionId(payment.getTransactionId());
    dto.setProcessedAt(payment.getProcessedAt());
    dto.setCreatedAt(payment.getCreatedAt());
    dto.setUpdatedAt(payment.getUpdatedAt());
    return dto;
  }

  /** Inner class for payment processing results */
  private static class PaymentResult {
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;

    public PaymentStatus getStatus() {
      return status;
    }

    public void setStatus(PaymentStatus status) {
      this.status = status;
    }

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    public String getFailureReason() {
      return failureReason;
    }

    public void setFailureReason(String failureReason) {
      this.failureReason = failureReason;
    }
  }

  /** Inner class for refund processing results */
  private static class RefundResult {
    private boolean successful;
    private String refundTransactionId;
    private String failureReason;

    public boolean isSuccessful() {
      return successful;
    }

    public void setSuccessful(boolean successful) {
      this.successful = successful;
    }

    public String getRefundTransactionId() {
      return refundTransactionId;
    }

    public void setRefundTransactionId(String refundTransactionId) {
      this.refundTransactionId = refundTransactionId;
    }

    public String getFailureReason() {
      return failureReason;
    }
  }
}
