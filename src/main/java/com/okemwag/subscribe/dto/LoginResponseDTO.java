package com.okemwag.subscribe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";
  private Long expiresIn;
  private BusinessDTO business;

  public LoginResponseDTO(
      String accessToken, String refreshToken, Long expiresIn, BusinessDTO business) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresIn = expiresIn;
    this.business = business;
  }
}
