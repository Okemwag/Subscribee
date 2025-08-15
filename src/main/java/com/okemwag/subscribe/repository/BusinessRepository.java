package com.okemwag.subscribe.repository;

import com.okemwag.subscribe.entity.Business;
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
public interface BusinessRepository extends JpaRepository<Business, Long> {
    
    // Basic tenant-aware queries
    Optional<Business> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT b FROM Business b WHERE b.active = true")
    List<Business> findAllActive();
    
    @Query("SELECT b FROM Business b WHERE b.id = :id AND b.active = true")
    Optional<Business> findByIdAndActive(@Param("id") Long id);
    
    // Enhanced tenant-aware queries
    @Query("SELECT b FROM Business b WHERE b.active = true ORDER BY b.createdAt DESC")
    Page<Business> findAllActiveWithPagination(Pageable pageable);
    
    @Query("SELECT b FROM Business b WHERE b.active = true AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Business> searchActiveBusinesses(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT b FROM Business b WHERE b.currency = :currency AND b.active = true")
    List<Business> findByCurrency(@Param("currency") String currency);
    
    @Query("SELECT b FROM Business b WHERE b.timezone = :timezone AND b.active = true")
    List<Business> findByTimezone(@Param("timezone") String timezone);
    
    @Query("SELECT b FROM Business b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate")
    List<Business> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Analytics queries for business insights
    @Query("SELECT COUNT(b) FROM Business b WHERE b.active = true")
    Long countActiveBusiness();
    
    @Query("SELECT COUNT(b) FROM Business b WHERE b.createdAt >= :startDate")
    Long countBusinessesCreatedSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT b.currency, COUNT(b) FROM Business b WHERE b.active = true GROUP BY b.currency")
    List<Object[]> countBusinessesByCurrency();
    
    @Query("SELECT b.timezone, COUNT(b) FROM Business b WHERE b.active = true GROUP BY b.timezone")
    List<Object[]> countBusinessesByTimezone();
}