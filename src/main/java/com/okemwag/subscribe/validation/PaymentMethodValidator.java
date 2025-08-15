package com.okemwag.subscribe.validation;

import com.okemwag.subscribe.util.EnumUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Validator for PaymentMethod enum values */
public class PaymentMethodValidator implements ConstraintValidator<ValidPaymentMethod, String> {

  @Override
  public void initialize(ValidPaymentMethod constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null validation
    }

    return EnumUtils.isValidPaymentMethod(value);
  }
}
