package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.PaymentDTO;
import com.okemwag.subscribe.dto.PaymentRequestDTO;
import com.okemwag.subscribe.dto.RefundDTO;
import com.okemwag.subscribe.dto.RefundRequestDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
  PaymentDTO processPayment(PaymentRequestDTO dto);

  PaymentDTO getPaymentById(Long paymentId);

  List<PaymentDTO> getPaymentHistory(Long customerId, Pageable pageable);

  RefundDTO processRefund(Long paymentId, RefundRequestDTO dto);

  List<PaymentDTO> getPaymentsBySubscription(Long subscriptionId);

  void processFailedPaymentRetries();
}
