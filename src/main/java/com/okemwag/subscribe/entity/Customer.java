package com.okemwag.subscribe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Entity
@Data
@Table(
    name = "customers",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "business_id"}))
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Customer name is required")
  @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  @Column(nullable = false, length = 255)
  private String email;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number format is invalid")
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  @Column(length = 20)
  private String phoneNumber;

  @NotNull(message = "Active status is required")
  @Column(nullable = false)
  private Boolean active = true;

  @NotBlank(message = "Preferred language is required")
  @Size(min = 2, max = 5, message = "Preferred language must be between 2 and 5 characters")
  @Pattern(
      regexp = "^[a-z]{2}(-[A-Z]{2})?$",
      message = "Preferred language must be in format 'en' or 'en-US'")
  @Column(nullable = false, length = 5)
  private String preferredLanguage = "en";

  @NotNull(message = "Business is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Subscription> subscriptions;

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
