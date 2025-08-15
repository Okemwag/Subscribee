package com.okemwag.subscribe.repository;

import com.okemwag.subscribe.entity.Invoice;
import com.okemwag.subscribe.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Basic invoice queries
    List<Invoice> findBySubscriptionId(Long subscriptionId);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
    
    // Business-scoped invoice queries
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId")
    List<Invoice> findByBusinessId(@Param("businessId") Long businessId);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.id = :invoiceId")
    Optional<Invoice> findByIdAndBusinessId(@Param("invoiceId") Long invoiceId, 
                                           @Param("businessId") Long businessId);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId ORDER BY i.createdAt DESC")
    Page<Invoice> findByBusinessIdOrderByCreatedAt(@Param("businessId") Long businessId, 
                                                  Pageable pageable);
    
    // Status-based queries
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = :status")
    List<Invoice> findByBusinessIdAndStatus(@Param("businessId") Long businessId, 
                                           @Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = :status ORDER BY i.createdAt DESC")
    Page<Invoice> findByBusinessIdAndStatusOrderByCreatedAt(@Param("businessId") Long businessId, 
                                                           @Param("status") InvoiceStatus status,
                                                           Pageable pageable);
    
    // Billing period queries
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.createdAt >= :startDate AND i.createdAt <= :endDate")
    List<Invoice> findByBusinessIdAndBillingPeriod(@Param("businessId") Long businessId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.dueDate >= :startDate AND i.dueDate <= :endDate")
    List<Invoice> findByBusinessIdAndDueDateBetween(@Param("businessId") Long businessId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.createdAt >= :startDate AND i.createdAt <= :endDate AND i.status = :status")
    List<Invoice> findByBusinessIdAndBillingPeriodAndStatus(@Param("businessId") Long businessId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           @Param("status") InvoiceStatus status);
    
    // Customer invoice history
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.id = :customerId ORDER BY i.createdAt DESC")
    Page<Invoice> findInvoiceHistoryByCustomer(@Param("customerId") Long customerId, 
                                              Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.subscription.customer.id = :customerId ORDER BY i.createdAt DESC")
    Page<Invoice> findInvoiceHistoryByCustomerAndBusiness(@Param("customerId") Long customerId,
                                                         @Param("businessId") Long businessId,
                                                         Pageable pageable);
    
    // Overdue invoice tracking
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoicesByBusiness(@Param("businessId") Long businessId, 
                                               @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findAllOverdueInvoices(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    Long countOverdueInvoicesByBusiness(@Param("businessId") Long businessId, 
                                       @Param("currentDate") LocalDateTime currentDate);
    
    // Analytics and reporting queries
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = 'PAID'")
    BigDecimal getTotalPaidAmountByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status NOT IN ('PAID', 'CANCELLED')")
    BigDecimal getTotalOutstandingAmountByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.createdAt >= :startDate AND i.createdAt <= :endDate")
    BigDecimal getTotalBilledAmountByBusinessAndPeriod(@Param("businessId") Long businessId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId")
    Long countInvoicesByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT i.status, COUNT(i) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId GROUP BY i.status")
    List<Object[]> countInvoicesByStatusAndBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.createdAt >= :startDate")
    Long countInvoicesCreatedSinceByBusiness(@Param("businessId") Long businessId, 
                                            @Param("startDate") LocalDateTime startDate);
    
    // Monthly billing analytics
    @Query("SELECT YEAR(i.createdAt), MONTH(i.createdAt), COUNT(i), SUM(i.totalAmount) FROM Invoice i " +
           "WHERE i.subscription.customer.business.id = :businessId " +
           "GROUP BY YEAR(i.createdAt), MONTH(i.createdAt) ORDER BY YEAR(i.createdAt), MONTH(i.createdAt)")
    List<Object[]> getMonthlyBillingStatsByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT YEAR(i.createdAt), MONTH(i.createdAt), SUM(i.totalAmount) FROM Invoice i " +
           "WHERE i.subscription.customer.business.id = :businessId AND i.status = 'PAID' " +
           "GROUP BY YEAR(i.createdAt), MONTH(i.createdAt) ORDER BY YEAR(i.createdAt), MONTH(i.createdAt)")
    List<Object[]> getMonthlyPaidAmountByBusiness(@Param("businessId") Long businessId);
    
    // Tax reporting
    @Query("SELECT SUM(i.taxAmount) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = 'PAID'")
    BigDecimal getTotalTaxCollectedByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT SUM(i.taxAmount) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = 'PAID' AND i.createdAt >= :startDate AND i.createdAt <= :endDate")
    BigDecimal getTaxCollectedByBusinessAndPeriod(@Param("businessId") Long businessId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    // Customer billing analysis
    @Query("SELECT c.id, c.name, COUNT(i), SUM(i.totalAmount) FROM Invoice i JOIN i.subscription s JOIN s.customer c " +
           "WHERE c.business.id = :businessId " +
           "GROUP BY c.id, c.name ORDER BY SUM(i.totalAmount) DESC")
    List<Object[]> getCustomerBillingStatsByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT c.id, c.name, SUM(i.totalAmount) FROM Invoice i JOIN i.subscription s JOIN s.customer c " +
           "WHERE c.business.id = :businessId AND i.status NOT IN ('PAID', 'CANCELLED') " +
           "GROUP BY c.id, c.name ORDER BY SUM(i.totalAmount) DESC")
    List<Object[]> getCustomersWithOutstandingBalancesByBusiness(@Param("businessId") Long businessId);
    
    // Subscription plan billing analysis
    @Query("SELECT sp.name, COUNT(i), SUM(i.totalAmount) FROM Invoice i JOIN i.subscription s JOIN s.subscriptionPlan sp " +
           "WHERE s.customer.business.id = :businessId " +
           "GROUP BY sp.name ORDER BY SUM(i.totalAmount) DESC")
    List<Object[]> getBillingStatsByPlanAndBusiness(@Param("businessId") Long businessId);
    
    // Payment collection efficiency
    @Query("SELECT AVG(DATEDIFF(p.processedAt, i.createdAt)) FROM Invoice i JOIN i.payments p " +
           "WHERE i.subscription.customer.business.id = :businessId AND i.status = 'PAID' AND p.status = 'COMPLETED'")
    Double getAveragePaymentDelayByBusiness(@Param("businessId") Long businessId);
    
    // Upcoming due invoices
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.dueDate BETWEEN :startDate AND :endDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findUpcomingDueInvoicesByBusiness(@Param("businessId") Long businessId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Draft invoice management
    @Query("SELECT i FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = 'DRAFT'")
    List<Invoice> findDraftInvoicesByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status = 'DRAFT'")
    Long countDraftInvoicesByBusiness(@Param("businessId") Long businessId);
    
    // Invoice aging analysis
    @Query("SELECT " +
           "SUM(CASE WHEN DATEDIFF(:currentDate, i.dueDate) <= 30 THEN i.totalAmount ELSE 0 END) as current, " +
           "SUM(CASE WHEN DATEDIFF(:currentDate, i.dueDate) BETWEEN 31 AND 60 THEN i.totalAmount ELSE 0 END) as thirtyDays, " +
           "SUM(CASE WHEN DATEDIFF(:currentDate, i.dueDate) BETWEEN 61 AND 90 THEN i.totalAmount ELSE 0 END) as sixtyDays, " +
           "SUM(CASE WHEN DATEDIFF(:currentDate, i.dueDate) > 90 THEN i.totalAmount ELSE 0 END) as ninetyDaysPlus " +
           "FROM Invoice i WHERE i.subscription.customer.business.id = :businessId AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Object[]> getInvoiceAgingAnalysisByBusiness(@Param("businessId") Long businessId, 
                                                    @Param("currentDate") LocalDateTime currentDate);
}