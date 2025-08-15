package com.okemwag.subscribe.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation for SubscriptionStatus enum values
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SubscriptionStatusValidator.class)
@Documented
public @interface ValidSubscriptionStatus {
    String message() default "Invalid subscription status. Valid values are: ACTIVE, CANCELLED, EXPIRED, SUSPENDED, TRIAL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}