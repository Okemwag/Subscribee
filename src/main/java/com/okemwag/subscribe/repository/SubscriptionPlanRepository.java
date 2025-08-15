package com.okemwag.subscribe.repository;

import com.okemwag.subscribe.entity.SubscriptionPlan;
import com.okemwag.subscribe.enums.BillingCycle;
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
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    
    // Basic business-scoped queries
    List<SubscriptionPlan> findByBusinessId(Long businessId);
    
    List<SubscriptionPlan> findByBusinessIdAndActive(Long businessId, Boolean active);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.id = :planId AND sp.active = true")
    Optional<SubscriptionPlan> findByIdAndBusinessIdAndActive(@Param("planId") Long planId, 
                                                             @Param("businessId") Long businessId);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.active = true ORDER BY sp.createdAt DESC")
    Page<SubscriptionPlan> findActiveByBusinessIdOrderByCreatedAt(@Param("businessId") Long businessId, 
                                                                 Pageable pageable);
    
    // Search and filtering
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.active = true AND " +
           "(LOWER(sp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sp.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<SubscriptionPlan> searchActiveByBusiness(@Param("businessId") Long businessId, 
                                                 @Param("searchTerm") String searchTerm, 
                                                 Pageable pageable);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.billingCycle = :billingCycle AND sp.active = true")
    List<SubscriptionPlan> findByBusinessIdAndBillingCycle(@Param("businessId") Long businessId, 
                                                          @Param("billingCycle") BillingCycle billingCycle);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.price BETWEEN :minPrice AND :maxPrice AND sp.active = true")
    List<SubscriptionPlan> findByBusinessIdAndPriceBetween(@Param("businessId") Long businessId,
                                                          @Param("minPrice") BigDecimal minPrice,
                                                          @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.trialDays > 0 AND sp.active = true")
    List<SubscriptionPlan> findByBusinessIdWithTrial(@Param("businessId") Long businessId);
    
    // Analytics queries
    @Query("SELECT COUNT(sp) FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.active = true")
    Long countActiveByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT sp.billingCycle, COUNT(sp) FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.active = true GROUP BY sp.billingCycle")
    List<Object[]> countByBillingCycleAndBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT COUNT(sp) FROM SubscriptionPlan sp WHERE sp.business.id = :businessId AND sp.createdAt >= :startDate")
    Long countCreatedSinceByBusiness(@Param("businessId") Long businessId, 
                                    @Param("startDate") LocalDateTime startDate);
    
    // Popular plans based on subscription count
    @Query("SELECT sp, COUNT(s) as subscriptionCount FROM SubscriptionPlan sp " +
           "LEFT JOIN sp.subscriptions s ON s.status = 'ACTIVE' " +
           "WHERE sp.business.id = :businessId AND sp.active = true " +
           "GROUP BY sp.id ORDER BY subscriptionCount DESC")
    List<Object[]> findPopularPlansByBusiness(@Param("businessId") Long businessId);
}