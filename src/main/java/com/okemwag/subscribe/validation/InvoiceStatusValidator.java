package com.okemwag.subscribe.validation;

import com.okemwag.subscribe.util.EnumUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Validator for InvoiceStatus enum values */
public class InvoiceStatusValidator implements ConstraintValidator<ValidInvoiceStatus, String> {

  @Override
  public void initialize(ValidInvoiceStatus constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null validation
    }

    return EnumUtils.isValidInvoiceStatus(value);
  }
}
