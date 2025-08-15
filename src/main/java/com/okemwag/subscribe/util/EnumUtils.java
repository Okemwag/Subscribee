package com.okemwag.subscribe.util;

import com.okemwag.subscribe.enums.*;
import com.okemwag.subscribe.exception.SubscribeException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for enum validation and conversion operations
 */
public class EnumUtils {

    /**
     * Convert string to SubscriptionStatus enum with validation
     * @param value String value to convert
     * @return SubscriptionStatus enum
     * @throws SubscribeException if value is invalid
     */
    public static SubscriptionStatus parseSubscriptionStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new SubscribeException("Subscription status cannot be null or empty");
        }
        
        try {
            return SubscriptionStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new SubscribeException("Invalid subscription status: " + value + 
                ". Valid values are: " + getValidSubscriptionStatuses());
        }
    }

    /**
     * Convert string to PaymentStatus enum with validation
     * @param value String value to convert
     * @return PaymentStatus enum
     * @throws SubscribeException if value is invalid
     */
    public static PaymentStatus parsePaymentStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new SubscribeException("Payment status cannot be null or empty");
        }
        
        try {
            return PaymentStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new SubscribeException("Invalid payment status: " + value + 
                ". Valid values are: " + getValidPaymentStatuses());
        }
    }

    /**
     * Convert string to PaymentMethod enum with validation
     * @param value String value to convert
     * @return PaymentMethod enum
     * @throws SubscribeException if value is invalid
     */
    public static PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new SubscribeException("Payment method cannot be null or empty");
        }
        
        try {
            return PaymentMethod.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new SubscribeException("Invalid payment method: " + value + 
                ". Valid values are: " + getValidPaymentMethods());
        }
    }

    /**
     * Convert string to BillingCycle enum with validation
     * @param value String value to convert
     * @return BillingCycle enum
     * @throws SubscribeException if value is invalid
     */
    public static BillingCycle parseBillingCycle(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new SubscribeException("Billing cycle cannot be null or empty");
        }
        
        try {
            return BillingCycle.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new SubscribeException("Invalid billing cycle: " + value + 
                ". Valid values are: " + getValidBillingCycles());
        }
    }

    /**
     * Convert string to InvoiceStatus enum with validation
     * @param value String value to convert
     * @return InvoiceStatus enum
     * @throws SubscribeException if value is invalid
     */
    public static InvoiceStatus parseInvoiceStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new SubscribeException("Invoice status cannot be null or empty");
        }
        
        try {
            return InvoiceStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new SubscribeException("Invalid invoice status: " + value + 
                ". Valid values are: " + getValidInvoiceStatuses());
        }
    }

    /**
     * Get all valid subscription status values as a comma-separated string
     * @return String of valid values
     */
    public static String getValidSubscriptionStatuses() {
        return Arrays.stream(SubscriptionStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all valid payment status values as a comma-separated string
     * @return String of valid values
     */
    public static String getValidPaymentStatuses() {
        return Arrays.stream(PaymentStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all valid payment method values as a comma-separated string
     * @return String of valid values
     */
    public static String getValidPaymentMethods() {
        return Arrays.stream(PaymentMethod.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all valid billing cycle values as a comma-separated string
     * @return String of valid values
     */
    public static String getValidBillingCycles() {
        return Arrays.stream(BillingCycle.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all valid invoice status values as a comma-separated string
     * @return String of valid values
     */
    public static String getValidInvoiceStatuses() {
        return Arrays.stream(InvoiceStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all valid subscription status values as a list
     * @return List of SubscriptionStatus values
     */
    public static List<SubscriptionStatus> getAllSubscriptionStatuses() {
        return Arrays.asList(SubscriptionStatus.values());
    }

    /**
     * Get all valid payment status values as a list
     * @return List of PaymentStatus values
     */
    public static List<PaymentStatus> getAllPaymentStatuses() {
        return Arrays.asList(PaymentStatus.values());
    }

    /**
     * Get all valid payment method values as a list
     * @return List of PaymentMethod values
     */
    public static List<PaymentMethod> getAllPaymentMethods() {
        return Arrays.asList(PaymentMethod.values());
    }

    /**
     * Get all valid billing cycle values as a list
     * @return List of BillingCycle values
     */
    public static List<BillingCycle> getAllBillingCycles() {
        return Arrays.asList(BillingCycle.values());
    }

    /**
     * Get all valid invoice status values as a list
     * @return List of InvoiceStatus values
     */
    public static List<InvoiceStatus> getAllInvoiceStatuses() {
        return Arrays.asList(InvoiceStatus.values());
    }

    /**
     * Check if a string is a valid subscription status
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSubscriptionStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            SubscriptionStatus.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid payment status
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPaymentStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            PaymentStatus.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid payment method
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPaymentMethod(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            PaymentMethod.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid billing cycle
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBillingCycle(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            BillingCycle.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid invoice status
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInvoiceStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            InvoiceStatus.valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}