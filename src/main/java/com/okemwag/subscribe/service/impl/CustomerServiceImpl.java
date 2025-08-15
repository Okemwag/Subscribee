package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.CustomerDTO;
import com.okemwag.subscribe.dto.CustomerRegistrationDTO;
import com.okemwag.subscribe.dto.CustomerUpdateDTO;
import com.okemwag.subscribe.entity.Business;
import com.okemwag.subscribe.entity.Customer;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.BusinessRepository;
import com.okemwag.subscribe.repository.CustomerRepository;
import com.okemwag.subscribe.service.interfaces.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;

    @Override
    public CustomerDTO registerCustomer(CustomerRegistrationDTO dto) {
        log.info("Registering new customer with email: {} for business ID: {}", 
            dto.getEmail(), dto.getBusinessId());
        
        // Validate business exists and is active
        Business business = businessRepository.findByIdAndActive(dto.getBusinessId())
            .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + dto.getBusinessId()));

        // Check if customer with email already exists within this business
        if (customerRepository.existsByEmailAndBusinessId(dto.getEmail(), dto.getBusinessId())) {
            throw new SubscribeException("Customer with email " + dto.getEmail() + 
                " already exists in this business");
        }

        // Create new customer entity
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setPreferredLanguage(dto.getPreferredLanguage());
        customer.setBusiness(business);
        customer.setActive(true);

        try {
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Successfully registered customer with ID: {} for business ID: {}", 
                savedCustomer.getId(), dto.getBusinessId());
            return convertToDTO(savedCustomer);
        } catch (Exception e) {
            log.error("Error registering customer: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to register customer", e);
        }
    }

    @Override
    public CustomerDTO updateCustomer(Long customerId, CustomerUpdateDTO dto) {
        log.info("Updating customer with ID: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Ensure customer is active
        if (!customer.getActive()) {
            throw new SubscribeException("Cannot update inactive customer");
        }

        // Check if email is being updated and if it already exists within the business
        if (dto.getEmail() != null && !dto.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmailAndBusinessId(dto.getEmail(), customer.getBusiness().getId())) {
                throw new SubscribeException("Customer with email " + dto.getEmail() + 
                    " already exists in this business");
            }
            customer.setEmail(dto.getEmail());
        }

        // Update other fields if provided
        if (dto.getName() != null) {
            customer.setName(dto.getName());
        }
        if (dto.getPhoneNumber() != null) {
            customer.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getPreferredLanguage() != null) {
            customer.setPreferredLanguage(dto.getPreferredLanguage());
        }

        try {
            Customer updatedCustomer = customerRepository.save(customer);
            log.info("Successfully updated customer with ID: {}", customerId);
            return convertToDTO(updatedCustomer);
        } catch (Exception e) {
            log.error("Error updating customer with ID {}: {}", customerId, e.getMessage(), e);
            throw new SubscribeException("Failed to update customer", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        log.debug("Retrieving customer with ID: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getActive()) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }
        
        return convertToDTO(customer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer with ID: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Soft delete by setting active to false
        customer.setActive(false);
        
        try {
            customerRepository.save(customer);
            log.info("Successfully deleted customer with ID: {}", customerId);
        } catch (Exception e) {
            log.error("Error deleting customer with ID {}: {}", customerId, e.getMessage(), e);
            throw new SubscribeException("Failed to delete customer", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByBusiness(Long businessId, Pageable pageable) {
        log.debug("Retrieving customers for business ID: {}", businessId);
        
        // Validate business exists and is active
        if (!businessRepository.findByIdAndActive(businessId).isPresent()) {
            throw new ResourceNotFoundException("Business not found with ID: " + businessId);
        }

        try {
            return customerRepository.findActiveCustomersByBusinessOrderByCreatedAt(businessId, pageable)
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving customers for business ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve customers", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(Long businessId, String searchTerm, Pageable pageable) {
        log.debug("Searching customers for business ID: {} with term: {}", businessId, searchTerm);
        
        // Validate business exists and is active
        if (!businessRepository.findByIdAndActive(businessId).isPresent()) {
            throw new ResourceNotFoundException("Business not found with ID: " + businessId);
        }

        try {
            return customerRepository.searchCustomersByBusiness(businessId, searchTerm, pageable)
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching customers for business ID {} with term {}: {}", 
                businessId, searchTerm, e.getMessage(), e);
            throw new SubscribeException("Failed to search customers", e);
        }
    }

    /**
     * Validates customer access for multi-tenant operations
     * Ensures that operations are performed within the correct business context
     */
    public void validateCustomerBusinessAccess(Long customerId, Long businessId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getBusiness().getId().equals(businessId)) {
            log.warn("Unauthorized access attempt: Business {} tried to access customer {} from business {}", 
                businessId, customerId, customer.getBusiness().getId());
            throw new SubscribeException("Access denied: Customer belongs to a different business");
        }
    }

    /**
     * Gets customer by ID within business scope for security
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByIdAndBusiness(Long customerId, Long businessId) {
        Customer customer = customerRepository.findByIdAndBusinessIdAndActive(customerId, businessId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Customer not found with ID: " + customerId + " in business: " + businessId));
        
        return convertToDTO(customer);
    }

    /**
     * Gets customer count for business analytics
     */
    @Transactional(readOnly = true)
    public Long getActiveCustomerCountByBusiness(Long businessId) {
        return customerRepository.countActiveCustomersByBusiness(businessId);
    }

    /**
     * Gets customers without subscriptions for business insights
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersWithoutSubscriptions(Long businessId) {
        // Validate business exists and is active
        if (!businessRepository.findByIdAndActive(businessId).isPresent()) {
            throw new ResourceNotFoundException("Business not found with ID: " + businessId);
        }

        return customerRepository.findCustomersWithoutSubscriptionsByBusiness(businessId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Converts Customer entity to CustomerDTO
     */
    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setActive(customer.getActive());
        dto.setPreferredLanguage(customer.getPreferredLanguage());
        dto.setBusinessId(customer.getBusiness().getId());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}