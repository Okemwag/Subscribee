package com.okemwag.subscribe.validation;

import com.okemwag.subscribe.util.EnumUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for PaymentStatus enum values
 */
public class PaymentStatusValidator implements ConstraintValidator<ValidPaymentStatus, String> {

    @Override
    public void initialize(ValidPaymentStatus constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        return EnumUtils.isValidPaymentStatus(value);
    }
}