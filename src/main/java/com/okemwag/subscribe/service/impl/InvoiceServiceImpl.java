package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.CreateInvoiceDTO;
import com.okemwag.subscribe.dto.InvoiceDTO;
import com.okemwag.subscribe.entity.Invoice;
import com.okemwag.subscribe.entity.Subscription;
import com.okemwag.subscribe.enums.InvoiceStatus;
import com.okemwag.subscribe.enums.SubscriptionStatus;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.InvoiceRepository;
import com.okemwag.subscribe.repository.SubscriptionRepository;
import com.okemwag.subscribe.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public InvoiceDTO createInvoice(CreateInvoiceDTO dto) {
        log.info("Creating invoice for subscription ID: {} with subtotal: {}", 
            dto.getSubscriptionId(), dto.getSubtotal());
        
        // Validate subscription exists and is active
        Subscription subscription = subscriptionRepository.findById(dto.getSubscriptionId())
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + dto.getSubscriptionId()));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && 
            subscription.getStatus() != SubscriptionStatus.TRIAL) {
            throw new SubscribeException("Cannot create invoice for inactive subscription");
        }

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setSubscription(subscription);
        invoice.setSubtotal(dto.getSubtotal());
        invoice.setTaxRate(dto.getTaxRate());
        invoice.setDueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDateTime.now().plusDays(30));
        invoice.setStatus(InvoiceStatus.DRAFT);

        // Tax and total will be calculated in @PrePersist
        try {
            Invoice savedInvoice = invoiceRepository.save(invoice);
            log.info("Successfully created invoice with ID: {} and number: {}", 
                savedInvoice.getId(), savedInvoice.getInvoiceNumber());
            return convertToDTO(savedInvoice);
        } catch (Exception e) {
            log.error("Error creating invoice: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to create invoice", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long invoiceId) {
        log.debug("Retrieving invoice with ID: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
        
        return convertToDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoiceHistory(Long customerId, Pageable pageable) {
        log.debug("Retrieving invoice history for customer ID: {}", customerId);
        
        try {
            return invoiceRepository.findInvoiceHistoryByCustomer(customerId, pageable)
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving invoice history for customer ID {}: {}", customerId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve invoice history", e);
        }
    }

    @Override
    public InvoiceDTO updateInvoiceStatus(Long invoiceId, InvoiceStatus status) {
        log.info("Updating invoice status for ID: {} to: {}", invoiceId, status);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Validate status transition
        if (!isValidStatusTransition(invoice.getStatus(), status)) {
            throw new SubscribeException("Invalid status transition from " + invoice.getStatus() + " to " + status);
        }

        invoice.setStatus(status);

        try {
            Invoice updatedInvoice = invoiceRepository.save(invoice);
            log.info("Successfully updated invoice status for ID: {}", invoiceId);
            return convertToDTO(updatedInvoice);
        } catch (Exception e) {
            log.error("Error updating invoice status for ID {}: {}", invoiceId, e.getMessage(), e);
            throw new SubscribeException("Failed to update invoice status", e);
        }
    }

    @Override
    public void generateAutomaticInvoices() {
        log.info("Generating automatic invoices for due subscriptions");
        
        try {
            // Find subscriptions due for billing
            LocalDateTime now = LocalDateTime.now();
            List<Subscription> subscriptionsDue = subscriptionRepository.findSubscriptionsDueForBilling(now);
            
            int invoicesGenerated = 0;
            for (Subscription subscription : subscriptionsDue) {
                try {
                    // Check if invoice already exists for this billing period
                    LocalDateTime billingStart = subscription.getNextBillingDate().minusMonths(1);
                    List<Invoice> existingInvoices = invoiceRepository.findByBusinessIdAndBillingPeriod(
                        subscription.getCustomer().getBusiness().getId(), billingStart, now);
                    
                    boolean invoiceExists = existingInvoices.stream()
                        .anyMatch(inv -> inv.getSubscription().getId().equals(subscription.getId()));
                    
                    if (!invoiceExists) {
                        // Create automatic invoice
                        CreateInvoiceDTO invoiceDTO = new CreateInvoiceDTO();
                        invoiceDTO.setSubscriptionId(subscription.getId());
                        invoiceDTO.setSubtotal(subscription.getSubscriptionPlan().getPrice());
                        invoiceDTO.setTaxRate(getBusinessTaxRate(subscription.getCustomer().getBusiness().getId()));
                        invoiceDTO.setDueDate(subscription.getNextBillingDate().plusDays(30));
                        
                        createInvoice(invoiceDTO);
                        invoicesGenerated++;
                        
                        log.info("Generated automatic invoice for subscription ID: {}", subscription.getId());
                    }
                    
                } catch (Exception e) {
                    log.error("Error generating invoice for subscription ID {}: {}", 
                        subscription.getId(), e.getMessage(), e);
                    // Continue with other subscriptions
                }
            }
            
            log.info("Generated {} automatic invoices", invoicesGenerated);
            
        } catch (Exception e) {
            log.error("Error generating automatic invoices: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to generate automatic invoices", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getOverdueInvoices(Long businessId) {
        log.debug("Retrieving overdue invoices for business ID: {}", businessId);
        
        try {
            return invoiceRepository.findOverdueInvoicesByBusiness(businessId, LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving overdue invoices for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve overdue invoices", e);
        }
    }

    @Override
    public void processOverdueInvoices() {
        log.info("Processing overdue invoices");
        
        try {
            List<Invoice> overdueInvoices = invoiceRepository.findAllOverdueInvoices(LocalDateTime.now());
            
            int processedCount = 0;
            for (Invoice invoice : overdueInvoices) {
                try {
                    if (invoice.getStatus() == InvoiceStatus.SENT) {
                        invoice.setStatus(InvoiceStatus.OVERDUE);
                        invoiceRepository.save(invoice);
                        processedCount++;
                        
                        log.info("Marked invoice {} as overdue", invoice.getInvoiceNumber());
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing overdue invoice ID {}: {}", invoice.getId(), e.getMessage(), e);
                    // Continue with other invoices
                }
            }
            
            log.info("Processed {} overdue invoices", processedCount);
            
        } catch (Exception e) {
            log.error("Error processing overdue invoices: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to process overdue invoices", e);
        }
    }

    /**
     * Gets invoices by business for multi-tenant operations
     */
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByBusiness(Long businessId, Pageable pageable) {
        log.debug("Retrieving invoices for business ID: {}", businessId);
        
        try {
            return invoiceRepository.findByBusinessIdOrderByCreatedAt(businessId, pageable)
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving invoices for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve business invoices", e);
        }
    }

    /**
     * Gets invoices by subscription
     */
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesBySubscription(Long subscriptionId) {
        log.debug("Retrieving invoices for subscription ID: {}", subscriptionId);
        
        // Validate subscription exists
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId);
        }

        try {
            return invoiceRepository.findBySubscriptionId(subscriptionId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving invoices for subscription ID {}: {}", subscriptionId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve subscription invoices", e);
        }
    }

    /**
     * Validates invoice access for multi-tenant operations
     */
    public void validateInvoiceBusinessAccess(Long invoiceId, Long businessId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
        
        if (!invoice.getSubscription().getCustomer().getBusiness().getId().equals(businessId)) {
            log.warn("Unauthorized access attempt: Business {} tried to access invoice {} from business {}", 
                businessId, invoiceId, invoice.getSubscription().getCustomer().getBusiness().getId());
            throw new SubscribeException("Access denied: Invoice belongs to a different business");
        }
    }

    /**
     * Marks invoice as paid when payment is received
     */
    public void markInvoiceAsPaid(Long invoiceId) {
        log.info("Marking invoice as paid: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            log.warn("Invoice {} is already marked as paid", invoiceId);
            return;
        }

        invoice.markAsPaid();
        invoiceRepository.save(invoice);
        
        log.info("Successfully marked invoice {} as paid", invoiceId);
    }

    /**
     * Gets business tax rate (mock implementation)
     */
    private BigDecimal getBusinessTaxRate(Long businessId) {
        // Mock implementation - in real scenario, this would be configurable per business
        return new BigDecimal("0.10"); // 10% tax rate
    }

    /**
     * Validates invoice status transitions
     */
    private boolean isValidStatusTransition(InvoiceStatus currentStatus, InvoiceStatus newStatus) {
        switch (currentStatus) {
            case DRAFT:
                return newStatus == InvoiceStatus.SENT || newStatus == InvoiceStatus.CANCELLED;
            case SENT:
                return newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.OVERDUE || 
                       newStatus == InvoiceStatus.CANCELLED;
            case OVERDUE:
                return newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.CANCELLED;
            case PAID:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }

    /**
     * Converts Invoice entity to InvoiceDTO
     */
    private InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setSubscriptionId(invoice.getSubscription().getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setStatus(invoice.getStatus());
        dto.setTaxRate(invoice.getTaxRate());
        dto.setDueDate(invoice.getDueDate());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setOverdue(invoice.isOverdue());
        return dto;
    }
}