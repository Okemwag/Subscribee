package com.okemwag.subscribe.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filter to set tenant context for multi-tenant data isolation */
@Component
@Order(2) // Execute after JWT authentication filter
@Slf4j
public class TenantIsolationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // Set tenant context from authenticated user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication != null
          && authentication.isAuthenticated()
          && authentication.getPrincipal() instanceof Long) {

        Long businessId = (Long) authentication.getPrincipal();
        String userEmail = (String) request.getAttribute("userEmail");

        TenantContext.setCurrentBusinessId(businessId);
        TenantContext.setCurrentUserEmail(userEmail);

        log.debug("Set tenant context - Business ID: {}, User: {}", businessId, userEmail);
      }

      filterChain.doFilter(request, response);

    } finally {
      // Always clear the context after request processing
      TenantContext.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    // Skip tenant isolation for public endpoints
    return path.startsWith("/api/v1/auth/")
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/v3/api-docs/")
        || path.startsWith("/actuator/health");
  }
}
