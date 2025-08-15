package com.okemwag.subscribe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRegistrationDTO {
  @NotBlank(message = "Customer name is required")
  @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  private String phoneNumber;

  @NotNull(message = "Business ID is required")
  private Long businessId;

  private String preferredLanguage = "en";
}
