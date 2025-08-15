package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.CreateInvoiceDTO;
import com.okemwag.subscribe.dto.InvoiceDTO;
import com.okemwag.subscribe.enums.InvoiceStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
  InvoiceDTO createInvoice(CreateInvoiceDTO dto);

  InvoiceDTO getInvoiceById(Long invoiceId);

  List<InvoiceDTO> getInvoiceHistory(Long customerId, Pageable pageable);

  InvoiceDTO updateInvoiceStatus(Long invoiceId, InvoiceStatus status);

  void generateAutomaticInvoices();

  List<InvoiceDTO> getOverdueInvoices(Long businessId);

  void processOverdueInvoices();
}
