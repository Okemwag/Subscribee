package com.okemwag.subscribe.security;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Component for logging security-related events and violations */
@Component
@Slf4j
public class SecurityAuditLogger {

  public void logSuccessfulLogin(String email, String ipAddress) {
    log.info(
        "SECURITY_AUDIT: Successful login - Email: {}, IP: {}, Timestamp: {}",
        email,
        ipAddress,
        LocalDateTime.now());
  }

  public void logFailedLogin(String email, String ipAddress, String reason) {
    log.warn(
        "SECURITY_AUDIT: Failed login attempt - Email: {}, IP: {}, Reason: {}, Timestamp: {}",
        email,
        ipAddress,
        reason,
        LocalDateTime.now());
  }

  public void logBusinessScopeViolation(
      Long currentBusinessId,
      Long requestedBusinessId,
      String userEmail,
      String method,
      String ipAddress) {
    log.error(
        "SECURITY_AUDIT: Business scope violation - Current Business: {}, "
            + "Requested Business: {}, User: {}, Method: {}, IP: {}, Timestamp: {}",
        currentBusinessId,
        requestedBusinessId,
        userEmail,
        method,
        ipAddress,
        LocalDateTime.now());
  }

  public void logUnauthorizedAccess(String endpoint, String userEmail, String ipAddress) {
    log.warn(
        "SECURITY_AUDIT: Unauthorized access attempt - Endpoint: {}, User: {}, IP: {}, Timestamp: {}",
        endpoint,
        userEmail,
        ipAddress,
        LocalDateTime.now());
  }

  public void logTokenValidationFailure(String token, String reason, String ipAddress) {
    // Don't log the full token for security reasons
    String tokenPrefix =
        token != null && token.length() > 10 ? token.substring(0, 10) + "..." : "null";
    log.warn(
        "SECURITY_AUDIT: Token validation failed - Token: {}, Reason: {}, IP: {}, Timestamp: {}",
        tokenPrefix,
        reason,
        ipAddress,
        LocalDateTime.now());
  }

  public void logSuspiciousActivity(
      String activity, String userEmail, String ipAddress, String details) {
    log.error(
        "SECURITY_AUDIT: Suspicious activity detected - Activity: {}, User: {}, IP: {}, "
            + "Details: {}, Timestamp: {}",
        activity,
        userEmail,
        ipAddress,
        details,
        LocalDateTime.now());
  }

  public void logDataAccess(String resource, String operation, String userEmail, Long businessId) {
    log.debug(
        "SECURITY_AUDIT: Data access - Resource: {}, Operation: {}, User: {}, Business: {}, Timestamp: {}",
        resource,
        operation,
        userEmail,
        businessId,
        LocalDateTime.now());
  }
}
