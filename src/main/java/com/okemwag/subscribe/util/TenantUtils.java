package com.okemwag.subscribe.util;

import com.okemwag.subscribe.exception.InsufficientPermissionException;
import com.okemwag.subscribe.security.TenantContext;

/** Utility class for tenant-aware operations */
public class TenantUtils {

  /**
   * Get current business ID from tenant context
   *
   * @throws InsufficientPermissionException if no business context is available
   */
  public static Long getCurrentBusinessId() {
    Long businessId = TenantContext.getCurrentBusinessId();
    if (businessId == null) {
      throw new InsufficientPermissionException("No business context available");
    }
    return businessId;
  }

  /** Get current business ID from tenant context, returning null if not available */
  public static Long getCurrentBusinessIdOrNull() {
    return TenantContext.getCurrentBusinessId();
  }

  /** Check if the provided business ID matches the current tenant context */
  public static boolean isCurrentBusiness(Long businessId) {
    Long currentBusinessId = TenantContext.getCurrentBusinessId();
    return currentBusinessId != null && currentBusinessId.equals(businessId);
  }

  /**
   * Validate that the provided business ID matches the current tenant context
   *
   * @throws InsufficientPermissionException if business IDs don't match
   */
  public static void validateBusinessAccess(Long businessId) {
    Long currentBusinessId = getCurrentBusinessId();
    if (!currentBusinessId.equals(businessId)) {
      throw new InsufficientPermissionException(
          "Access denied: insufficient permissions for business " + businessId);
    }
  }

  /** Get current user email from tenant context */
  public static String getCurrentUserEmail() {
    return TenantContext.getCurrentUserEmail();
  }

  /** Check if there is an active tenant context */
  public static boolean hasTenantContext() {
    return TenantContext.hasCurrentBusiness();
  }
}
