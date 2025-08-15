package com.okemwag.subscribe.validation;

import com.okemwag.subscribe.util.EnumUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for SubscriptionStatus enum values
 */
public class SubscriptionStatusValidator implements ConstraintValidator<ValidSubscriptionStatus, String> {

    @Override
    public void initialize(ValidSubscriptionStatus constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        return EnumUtils.isValidSubscriptionStatus(value);
    }
}