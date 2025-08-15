package com.okemwag.subscribe.security;

/** Thread-local context to store current tenant (business) information */
public class TenantContext {

  private static final ThreadLocal<Long> currentBusinessId = new ThreadLocal<>();
  private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();

  public static void setCurrentBusinessId(Long businessId) {
    currentBusinessId.set(businessId);
  }

  public static Long getCurrentBusinessId() {
    return currentBusinessId.get();
  }

  public static void setCurrentUserEmail(String email) {
    currentUserEmail.set(email);
  }

  public static String getCurrentUserEmail() {
    return currentUserEmail.get();
  }

  public static void clear() {
    currentBusinessId.remove();
    currentUserEmail.remove();
  }

  public static boolean hasCurrentBusiness() {
    return currentBusinessId.get() != null;
  }
}
