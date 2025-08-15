package com.okemwag.subscribe.util;

import static org.junit.jupiter.api.Assertions.*;

import com.okemwag.subscribe.enums.*;
import com.okemwag.subscribe.exception.SubscribeException;
import org.junit.jupiter.api.Test;

class EnumUtilsTest {

  @Test
  void testParseSubscriptionStatus_ValidValues() {
    assertEquals(SubscriptionStatus.ACTIVE, EnumUtils.parseSubscriptionStatus("ACTIVE"));
    assertEquals(SubscriptionStatus.ACTIVE, EnumUtils.parseSubscriptionStatus("active"));
    assertEquals(SubscriptionStatus.ACTIVE, EnumUtils.parseSubscriptionStatus(" ACTIVE "));
    assertEquals(SubscriptionStatus.CANCELLED, EnumUtils.parseSubscriptionStatus("CANCELLED"));
    assertEquals(SubscriptionStatus.EXPIRED, EnumUtils.parseSubscriptionStatus("EXPIRED"));
    assertEquals(SubscriptionStatus.SUSPENDED, EnumUtils.parseSubscriptionStatus("SUSPENDED"));
    assertEquals(SubscriptionStatus.TRIAL, EnumUtils.parseSubscriptionStatus("TRIAL"));
  }

  @Test
  void testParseSubscriptionStatus_InvalidValues() {
    assertThrows(SubscribeException.class, () -> EnumUtils.parseSubscriptionStatus("INVALID"));
    assertThrows(SubscribeException.class, () -> EnumUtils.parseSubscriptionStatus(""));
    assertThrows(SubscribeException.class, () -> EnumUtils.parseSubscriptionStatus(null));
    assertThrows(SubscribeException.class, () -> EnumUtils.parseSubscriptionStatus("   "));
  }

  @Test
  void testParsePaymentStatus_ValidValues() {
    assertEquals(PaymentStatus.PENDING, EnumUtils.parsePaymentStatus("PENDING"));
    assertEquals(PaymentStatus.COMPLETED, EnumUtils.parsePaymentStatus("completed"));
    assertEquals(PaymentStatus.FAILED, EnumUtils.parsePaymentStatus(" FAILED "));
    assertEquals(PaymentStatus.REFUNDED, EnumUtils.parsePaymentStatus("REFUNDED"));
    assertEquals(PaymentStatus.CANCELLED, EnumUtils.parsePaymentStatus("CANCELLED"));
  }

  @Test
  void testParsePaymentMethod_ValidValues() {
    assertEquals(PaymentMethod.STRIPE_CARD, EnumUtils.parsePaymentMethod("STRIPE_CARD"));
    assertEquals(PaymentMethod.MPESA, EnumUtils.parsePaymentMethod("mpesa"));
    assertEquals(PaymentMethod.BANK_TRANSFER, EnumUtils.parsePaymentMethod(" BANK_TRANSFER "));
  }

  @Test
  void testParseBillingCycle_ValidValues() {
    assertEquals(BillingCycle.MONTHLY, EnumUtils.parseBillingCycle("MONTHLY"));
    assertEquals(BillingCycle.QUARTERLY, EnumUtils.parseBillingCycle("quarterly"));
    assertEquals(BillingCycle.YEARLY, EnumUtils.parseBillingCycle(" YEARLY "));
  }

  @Test
  void testParseInvoiceStatus_ValidValues() {
    assertEquals(InvoiceStatus.DRAFT, EnumUtils.parseInvoiceStatus("DRAFT"));
    assertEquals(InvoiceStatus.SENT, EnumUtils.parseInvoiceStatus("sent"));
    assertEquals(InvoiceStatus.PAID, EnumUtils.parseInvoiceStatus(" PAID "));
    assertEquals(InvoiceStatus.OVERDUE, EnumUtils.parseInvoiceStatus("OVERDUE"));
    assertEquals(InvoiceStatus.CANCELLED, EnumUtils.parseInvoiceStatus("CANCELLED"));
  }

  @Test
  void testValidationMethods() {
    assertTrue(EnumUtils.isValidSubscriptionStatus("ACTIVE"));
    assertTrue(EnumUtils.isValidSubscriptionStatus("active"));
    assertFalse(EnumUtils.isValidSubscriptionStatus("INVALID"));
    assertFalse(EnumUtils.isValidSubscriptionStatus(null));
    assertFalse(EnumUtils.isValidSubscriptionStatus(""));

    assertTrue(EnumUtils.isValidPaymentStatus("PENDING"));
    assertFalse(EnumUtils.isValidPaymentStatus("INVALID"));

    assertTrue(EnumUtils.isValidPaymentMethod("STRIPE_CARD"));
    assertFalse(EnumUtils.isValidPaymentMethod("INVALID"));

    assertTrue(EnumUtils.isValidBillingCycle("MONTHLY"));
    assertFalse(EnumUtils.isValidBillingCycle("INVALID"));

    assertTrue(EnumUtils.isValidInvoiceStatus("DRAFT"));
    assertFalse(EnumUtils.isValidInvoiceStatus("INVALID"));
  }

  @Test
  void testGetAllMethods() {
    assertEquals(5, EnumUtils.getAllSubscriptionStatuses().size());
    assertEquals(5, EnumUtils.getAllPaymentStatuses().size());
    assertEquals(3, EnumUtils.getAllPaymentMethods().size());
    assertEquals(3, EnumUtils.getAllBillingCycles().size());
    assertEquals(5, EnumUtils.getAllInvoiceStatuses().size());
  }

  @Test
  void testGetValidStringMethods() {
    String subscriptionStatuses = EnumUtils.getValidSubscriptionStatuses();
    assertTrue(subscriptionStatuses.contains("ACTIVE"));
    assertTrue(subscriptionStatuses.contains("CANCELLED"));
    assertTrue(subscriptionStatuses.contains("EXPIRED"));
    assertTrue(subscriptionStatuses.contains("SUSPENDED"));
    assertTrue(subscriptionStatuses.contains("TRIAL"));

    String paymentStatuses = EnumUtils.getValidPaymentStatuses();
    assertTrue(paymentStatuses.contains("PENDING"));
    assertTrue(paymentStatuses.contains("COMPLETED"));
    assertTrue(paymentStatuses.contains("FAILED"));
    assertTrue(paymentStatuses.contains("REFUNDED"));
    assertTrue(paymentStatuses.contains("CANCELLED"));
  }
}
