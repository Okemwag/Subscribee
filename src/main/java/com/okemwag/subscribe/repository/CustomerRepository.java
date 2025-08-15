package com.okemwag.subscribe.repository;

import com.okemwag.subscribe.entity.Customer;
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
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  // Basic business-scoped queries
  Optional<Customer> findByEmailAndBusinessId(String email, Long businessId);

  boolean existsByEmailAndBusinessId(String email, Long businessId);

  Page<Customer> findByBusinessIdAndActive(Long businessId, Boolean active, Pageable pageable);

  List<Customer> findByBusinessId(Long businessId);

  // Enhanced business-scoped search and filtering
  @Query(
      "SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.active = true AND "
          + "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<Customer> searchCustomersByBusiness(
      @Param("businessId") Long businessId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query(
      "SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.id = :customerId AND c.active = true")
  Optional<Customer> findByIdAndBusinessIdAndActive(
      @Param("customerId") Long customerId, @Param("businessId") Long businessId);

  @Query(
      "SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.preferredLanguage = :language AND c.active = true")
  List<Customer> findByBusinessIdAndPreferredLanguage(
      @Param("businessId") Long businessId, @Param("language") String language);

  @Query(
      "SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.createdAt >= :startDate AND c.createdAt <= :endDate")
  List<Customer> findByBusinessIdAndCreatedAtBetween(
      @Param("businessId") Long businessId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  // Business-scoped analytics queries
  @Query("SELECT COUNT(c) FROM Customer c WHERE c.business.id = :businessId AND c.active = true")
  Long countActiveCustomersByBusiness(@Param("businessId") Long businessId);

  @Query("SELECT COUNT(c) FROM Customer c WHERE c.business.id = :businessId")
  Long countAllCustomersByBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT COUNT(c) FROM Customer c WHERE c.business.id = :businessId AND c.createdAt >= :startDate")
  Long countCustomersCreatedSinceByBusiness(
      @Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate);

  @Query(
      "SELECT c.preferredLanguage, COUNT(c) FROM Customer c WHERE c.business.id = :businessId AND c.active = true GROUP BY c.preferredLanguage")
  List<Object[]> countCustomersByLanguageAndBusiness(@Param("businessId") Long businessId);

  // Multi-tenant safe queries - ensure customers can only access their own business data
  @Query(
      "SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.active = true ORDER BY c.createdAt DESC")
  Page<Customer> findActiveCustomersByBusinessOrderByCreatedAt(
      @Param("businessId") Long businessId, Pageable pageable);

  @Query(
      "SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.phoneNumber IS NOT NULL AND c.active = true")
  List<Customer> findCustomersWithPhoneByBusiness(@Param("businessId") Long businessId);

  // Customer engagement queries
  @Query(
      "SELECT c FROM Customer c LEFT JOIN c.subscriptions s WHERE c.business.id = :businessId AND c.active = true "
          + "GROUP BY c.id HAVING COUNT(s) = 0")
  List<Customer> findCustomersWithoutSubscriptionsByBusiness(@Param("businessId") Long businessId);

  @Query(
      "SELECT c FROM Customer c LEFT JOIN c.subscriptions s WHERE c.business.id = :businessId AND c.active = true "
          + "GROUP BY c.id HAVING COUNT(s) > 0")
  List<Customer> findCustomersWithSubscriptionsByBusiness(@Param("businessId") Long businessId);
}
