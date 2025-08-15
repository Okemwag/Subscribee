package com.okemwag.subscribe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {

  // Use a strong secret key - should be overridden in production via environment variables
  private String secret =
      "subscribe-jwt-secret-key-that-should-be-changed-in-production-and-be-at-least-256-bits-long";

  // Token expiration time in milliseconds (24 hours)
  private long expiration = 86400000;

  // Refresh token expiration time in milliseconds (7 days)
  private long refreshExpiration = 604800000;
}
