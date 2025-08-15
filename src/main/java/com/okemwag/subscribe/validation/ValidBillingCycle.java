package com.okemwag.subscribe.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Validation annotation for BillingCycle enum values */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BillingCycleValidator.class)
@Documented
public @interface ValidBillingCycle {
  String message() default "Invalid billing cycle. Valid values are: MONTHLY, QUARTERLY, YEARLY";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
