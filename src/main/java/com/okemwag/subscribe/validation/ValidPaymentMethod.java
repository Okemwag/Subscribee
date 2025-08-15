package com.okemwag.subscribe.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Validation annotation for PaymentMethod enum values */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentMethodValidator.class)
@Documented
public @interface ValidPaymentMethod {
  String message() default
      "Invalid payment method. Valid values are: STRIPE_CARD, MPESA, BANK_TRANSFER";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
