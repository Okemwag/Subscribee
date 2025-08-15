package com.okemwag.subscribe.repository;

import com.okemwag.subscribe.entity.Payment;
import com.okemwag.subscribe.enums.PaymentMethod;
import com.okemwag.subscribe.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  // Basic payment queries
  List<Payment> findBySubscriptionId(Long subscriptionId);

  List<Payment> findByStatus(PaymentStatus status);

  List<Payment> findByMethod(PaymentMethod method);

  Optional<Payment> findByTransactionId(String transactionId);

  // Business-scoped payment queries
  @Query("SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId")
  List<Payment> findByBusinessId(@Param("businessId") Long businessId);

  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.id = :paymentId")
  Optional<Payment> findByIdAndBusinessId(
      @Param("paymentId") Long paymentId, @Param("businessId") Long businessId);

  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId ORDER BY p.createdAt DESC")
  Page<Payment> findByBusinessIdOrderByCreatedAt(
      @Param("businessId") Long businessId, Pageable pageable);

  // Transaction history queries
  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.id = :customerId ORDER BY p.createdAt DESC")
  Page<Payment> findPaymentHistoryByCustomer(
      @Param("customerId") Long customerId, Pageable pageable);

  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.subscription.customer.id = :customerId ORDER BY p.createdAt DESC")
  Page<Payment> findPaymentHistoryByCustomerAndBusiness(
      @Param("customerId") Long customerId,
      @Param("businessId") Long businessId,
      Pageable pageable);

  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId ORDER BY p.createdAt DESC")
  List<Payment> findPaymentHistoryBySubscription(@Param("subscriptionId") Long subscriptionId);

  // Status-based queries
  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = :status")
  List<Payment> findByBusinessIdAndStatus(
      @Param("businessId") Long businessId, @Param("status") PaymentStatus status);

  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = :status ORDER BY p.createdAt DESC")
  Page<Payment> findByBusinessIdAndStatusOrderByCreatedAt(
      @Param("businessId") Long businessId,
      @Param("status") PaymentStatus status,
      Pageable pageable);

  @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt <= :cutoffTime")
  List<Payment> findStalePaymentsByStatus(
      @Param("status") PaymentStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);

  // Date-based queries
  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
  List<Payment> findByBusinessIdAndCreatedAtBetween(
      @Param("businessId") Long businessId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.processedAt >= :startDate AND p.processedAt <= :endDate")
  List<Payment> findByBusinessIdAndProcessedAtBetween(
      @Param("businessId") Long businessId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  // Analytics and reporting queries
  @Query(
      "SELECT SUM(p.amount) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED'")
  BigDecimal getTotalRevenueByBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT SUM(p.amount) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED' AND p.processedAt >= :startDate AND p.processedAt <= :endDate")
  BigDecimal getRevenueByBusinessAndDateRange(
      @Param("businessId") Long businessId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT COUNT(p) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED'")
  Long countSuccessfulPaymentsByBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT COUNT(p) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'FAILED'")
  Long countFailedPaymentsByBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT p.status, COUNT(p) FROM Payment p WHERE p.subscription.customer.business.id = :businessId GROUP BY p.status")
  List<Object[]> countPaymentsByStatusAndBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT p.method, COUNT(p) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED' GROUP BY p.method")
  List<Object[]> countSuccessfulPaymentsByMethodAndBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT p.currency, SUM(p.amount) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED' GROUP BY p.currency")
  List<Object[]> getRevenueByCurrencyAndBusiness(@Param("businessId") Long businessId);

  // Monthly revenue analytics
  @Query(
      "SELECT YEAR(p.processedAt), MONTH(p.processedAt), SUM(p.amount) FROM Payment p "
          + "WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED' "
          + "GROUP BY YEAR(p.processedAt), MONTH(p.processedAt) ORDER BY YEAR(p.processedAt), MONTH(p.processedAt)")
  List<Object[]> getMonthlyRevenueByBusiness(@Param("businessId") Long businessId);

  // Failed payment analysis
  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'FAILED' ORDER BY p.createdAt DESC")
  Page<Payment> findFailedPaymentsByBusiness(
      @Param("businessId") Long businessId, Pageable pageable);

  @Query(
      "SELECT COUNT(p) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'FAILED' AND p.createdAt >= :startDate")
  Long countRecentFailedPaymentsByBusiness(
      @Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate);

  // Payment method analysis
  @Query(
      "SELECT p.method, AVG(p.amount) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED' GROUP BY p.method")
  List<Object[]> getAveragePaymentAmountByMethodAndBusiness(@Param("businessId") Long businessId);

  // Customer payment behavior
  @Query(
      "SELECT c.id, c.name, COUNT(p), SUM(p.amount) FROM Payment p JOIN p.subscription s JOIN s.customer c "
          + "WHERE c.business.id = :businessId AND p.status = 'COMPLETED' "
          + "GROUP BY c.id, c.name ORDER BY SUM(p.amount) DESC")
  List<Object[]> getTopPayingCustomersByBusiness(@Param("businessId") Long businessId);

  // Refund tracking
  @Query(
      "SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'REFUNDED'")
  List<Payment> findRefundedPaymentsByBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT SUM(p.amount) FROM Payment p WHERE p.subscription.customer.business.id = :businessId AND p.status = 'REFUNDED'")
  BigDecimal getTotalRefundsByBusiness(@Param("businessId") Long businessId);
}
