package com.okemwag.subscribe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessRegistrationDTO {
  @NotBlank(message = "Business name is required")
  @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  private String phoneNumber;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  private String password;

  private String timezone = "UTC";
  private String currency = "USD";
}
