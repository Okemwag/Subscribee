package com.okemwag.subscribe.repository;

import com.okemwag.subscribe.entity.Subscription;
import com.okemwag.subscribe.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Basic queries
    List<Subscription> findByCustomerId(Long customerId);
    
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    List<Subscription> findByCustomerBusinessId(Long businessId);
    
    // Enhanced status-based queries
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = :status")
    List<Subscription> findByBusinessIdAndStatus(@Param("businessId") Long businessId, 
                                                @Param("status") SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = :status")
    Page<Subscription> findByBusinessIdAndStatusWithPagination(@Param("businessId") Long businessId, 
                                                              @Param("status") SubscriptionStatus status,
                                                              Pageable pageable);
    
    @Query("SELECT s FROM Subscription s WHERE s.customer.id = :customerId AND s.status = :status")
    List<Subscription> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                                @Param("status") SubscriptionStatus status);
    
    // Date-based queries for billing and lifecycle management
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate <= :date AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsDueForBilling(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.nextBillingDate <= :date AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsDueForBillingByBusiness(@Param("businessId") Long businessId, 
                                                               @Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate <= :date AND s.status IN ('ACTIVE', 'TRIAL')")
    List<Subscription> findExpiredSubscriptions(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.endDate <= :date AND s.status IN ('ACTIVE', 'TRIAL')")
    List<Subscription> findExpiredSubscriptionsByBusiness(@Param("businessId") Long businessId, 
                                                         @Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.startDate >= :startDate AND s.startDate <= :endDate")
    List<Subscription> findByStartDateBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.startDate >= :startDate AND s.startDate <= :endDate")
    List<Subscription> findByBusinessIdAndStartDateBetween(@Param("businessId") Long businessId,
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    // Business-scoped subscription management
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.id = :subscriptionId")
    Optional<Subscription> findByIdAndBusinessId(@Param("subscriptionId") Long subscriptionId, 
                                                @Param("businessId") Long businessId);
    
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId ORDER BY s.createdAt DESC")
    Page<Subscription> findByBusinessIdOrderByCreatedAt(@Param("businessId") Long businessId, 
                                                       Pageable pageable);
    
    // Analytics and reporting queries
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = 'ACTIVE'")
    Long countActiveSubscriptionsByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId")
    Long countAllSubscriptionsByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT s.status, COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId GROUP BY s.status")
    List<Object[]> countSubscriptionsByStatusAndBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId AND s.createdAt >= :startDate")
    Long countSubscriptionsCreatedSinceByBusiness(@Param("businessId") Long businessId, 
                                                 @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT sp.name, COUNT(s) FROM Subscription s JOIN s.subscriptionPlan sp WHERE s.customer.business.id = :businessId AND s.status = 'ACTIVE' GROUP BY sp.name")
    List<Object[]> countActiveSubscriptionsByPlanAndBusiness(@Param("businessId") Long businessId);
    
    // Trial and conversion tracking
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = 'TRIAL' AND s.endDate <= :date")
    List<Subscription> findTrialSubscriptionsEndingByBusiness(@Param("businessId") Long businessId, 
                                                             @Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = 'TRIAL'")
    Long countTrialSubscriptionsByBusiness(@Param("businessId") Long businessId);
    
    // Churn analysis
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = 'CANCELLED' AND s.endDate >= :startDate AND s.endDate <= :endDate")
    List<Subscription> findCancelledSubscriptionsByBusinessAndDateRange(@Param("businessId") Long businessId,
                                                                       @Param("startDate") LocalDateTime startDate,
                                                                       @Param("endDate") LocalDateTime endDate);
    
    // Renewal tracking
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.nextBillingDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsForRenewalByBusinessAndDateRange(@Param("businessId") Long businessId,
                                                                        @Param("startDate") LocalDateTime startDate,
                                                                        @Param("endDate") LocalDateTime endDate);
}