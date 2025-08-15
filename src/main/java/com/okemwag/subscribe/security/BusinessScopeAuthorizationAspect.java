package com.okemwag.subscribe.security;

import com.okemwag.subscribe.exception.InsufficientPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Aspect to enforce business-scoped authorization checks
 */
@Aspect
@Component
@Slf4j
public class BusinessScopeAuthorizationAspect {

    @Before("@annotation(businessScopeCheck)")
    public void checkBusinessScope(JoinPoint joinPoint, BusinessScopeCheck businessScopeCheck) {
        Long currentBusinessId = TenantContext.getCurrentBusinessId();
        
        if (currentBusinessId == null) {
            log.warn("No business context found for method: {}", joinPoint.getSignature().getName());
            throw new InsufficientPermissionException("No business context available");
        }

        // Get the business ID parameter from method arguments
        Object[] args = joinPoint.getArgs();
        Long requestedBusinessId = null;
        
        // Look for business ID in method parameters
        for (Object arg : args) {
            if (arg instanceof Long && businessScopeCheck.parameterName().equals("businessId")) {
                requestedBusinessId = (Long) arg;
                break;
            }
            // Handle DTOs that might contain business ID
            if (arg != null && hasBusinessIdField(arg)) {
                requestedBusinessId = extractBusinessId(arg);
                break;
            }
        }

        // If we found a business ID in parameters, verify it matches current context
        if (requestedBusinessId != null && !requestedBusinessId.equals(currentBusinessId)) {
            log.warn("Business scope violation - Current: {}, Requested: {}, Method: {}", 
                    currentBusinessId, requestedBusinessId, joinPoint.getSignature().getName());
            throw new InsufficientPermissionException("Access denied: insufficient permissions for this business");
        }

        log.debug("Business scope check passed for business: {}", currentBusinessId);
    }

    private boolean hasBusinessIdField(Object obj) {
        try {
            obj.getClass().getDeclaredField("businessId");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private Long extractBusinessId(Object obj) {
        try {
            var field = obj.getClass().getDeclaredField("businessId");
            field.setAccessible(true);
            return (Long) field.get(obj);
        } catch (Exception e) {
            log.warn("Could not extract business ID from object: {}", obj.getClass().getSimpleName());
            return null;
        }
    }
}