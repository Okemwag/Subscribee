package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.CustomerDTO;
import com.okemwag.subscribe.dto.CustomerRegistrationDTO;
import com.okemwag.subscribe.dto.CustomerUpdateDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    CustomerDTO registerCustomer(CustomerRegistrationDTO dto);
    CustomerDTO updateCustomer(Long customerId, CustomerUpdateDTO dto);
    CustomerDTO getCustomerById(Long customerId);
    void deleteCustomer(Long customerId);
    List<CustomerDTO> getCustomersByBusiness(Long businessId, Pageable pageable);
    List<CustomerDTO> searchCustomers(Long businessId, String searchTerm, Pageable pageable);
}