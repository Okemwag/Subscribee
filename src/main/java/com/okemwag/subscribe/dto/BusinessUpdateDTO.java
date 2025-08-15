package com.okemwag.subscribe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessUpdateDTO {
  @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
  private String name;

  @Email(message = "Email should be valid")
  private String email;

  private String phoneNumber;
  private String timezone;
  private String currency;
}
