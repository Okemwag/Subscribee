package com.okemwag.subscribe.validation;

import com.okemwag.subscribe.util.EnumUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for BillingCycle enum values
 */
public class BillingCycleValidator implements ConstraintValidator<ValidBillingCycle, String> {

    @Override
    public void initialize(ValidBillingCycle constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        return EnumUtils.isValidBillingCycle(value);
    }
}