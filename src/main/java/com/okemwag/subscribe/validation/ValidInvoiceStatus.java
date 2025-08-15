package com.okemwag.subscribe.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Validation annotation for InvoiceStatus enum values */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InvoiceStatusValidator.class)
@Documented
public @interface ValidInvoiceStatus {
  String message() default
      "Invalid invoice status. Valid values are: DRAFT, SENT, PAID, OVERDUE, CANCELLED";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
