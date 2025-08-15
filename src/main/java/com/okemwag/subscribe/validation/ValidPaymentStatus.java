package com.okemwag.subscribe.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Validation annotation for PaymentStatus enum values */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentStatusValidator.class)
@Documented
public @interface ValidPaymentStatus {
  String message() default
      "Invalid payment status. Valid values are: PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
