package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.BusinessDTO;
import com.okemwag.subscribe.dto.BusinessRegistrationDTO;
import com.okemwag.subscribe.dto.BusinessUpdateDTO;
import com.okemwag.subscribe.entity.Business;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.BusinessRepository;
import com.okemwag.subscribe.service.interfaces.BusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public BusinessDTO registerBusiness(BusinessRegistrationDTO dto) {
        log.info("Registering new business with email: {}", dto.getEmail());
        
        // Check if business with email already exists
        if (businessRepository.existsByEmail(dto.getEmail())) {
            throw new SubscribeException("Business with email " + dto.getEmail() + " already exists");
        }

        // Create new business entity
        Business business = new Business();
        business.setName(dto.getName());
        business.setEmail(dto.getEmail());
        business.setPhoneNumber(dto.getPhoneNumber());
        business.setPassword(passwordEncoder.encode(dto.getPassword()));
        business.setTimezone(dto.getTimezone());
        business.setCurrency(dto.getCurrency());
        business.setActive(true);

        try {
            Business savedBusiness = businessRepository.save(business);
            log.info("Successfully registered business with ID: {}", savedBusiness.getId());
            return convertToDTO(savedBusiness);
        } catch (Exception e) {
            log.error("Error registering business: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to register business", e);
        }
    }

    @Override
    public BusinessDTO updateBusiness(Long businessId, BusinessUpdateDTO dto) {
        log.info("Updating business with ID: {}", businessId);
        
        Business business = businessRepository.findByIdAndActive(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check if email is being updated and if it already exists
        if (dto.getEmail() != null && !dto.getEmail().equals(business.getEmail())) {
            if (businessRepository.existsByEmail(dto.getEmail())) {
                throw new SubscribeException("Business with email " + dto.getEmail() + " already exists");
            }
            business.setEmail(dto.getEmail());
        }

        // Update other fields if provided
        if (dto.getName() != null) {
            business.setName(dto.getName());
        }
        if (dto.getPhoneNumber() != null) {
            business.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getTimezone() != null) {
            business.setTimezone(dto.getTimezone());
        }
        if (dto.getCurrency() != null) {
            business.setCurrency(dto.getCurrency());
        }

        try {
            Business updatedBusiness = businessRepository.save(business);
            log.info("Successfully updated business with ID: {}", businessId);
            return convertToDTO(updatedBusiness);
        } catch (Exception e) {
            log.error("Error updating business with ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to update business", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessDTO getBusinessById(Long businessId) {
        log.debug("Retrieving business with ID: {}", businessId);
        
        Business business = businessRepository.findByIdAndActive(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));
        
        return convertToDTO(business);
    }

    @Override
    public void deleteBusiness(Long businessId) {
        log.info("Deleting business with ID: {}", businessId);
        
        Business business = businessRepository.findByIdAndActive(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Soft delete by setting active to false
        business.setActive(false);
        
        try {
            businessRepository.save(business);
            log.info("Successfully deleted business with ID: {}", businessId);
        } catch (Exception e) {
            log.error("Error deleting business with ID {}: {}", businessId, e.getMessage(), e);
            throw new SubscribeException("Failed to delete business", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessDTO> getAllBusinesses(Pageable pageable) {
        log.debug("Retrieving all active businesses with pagination");
        
        try {
            return businessRepository.findAllActiveWithPagination(pageable)
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving businesses: {}", e.getMessage(), e);
            throw new SubscribeException("Failed to retrieve businesses", e);
        }
    }

    /**
     * Validates business access for multi-tenant operations
     * This method ensures that operations are performed within the correct business context
     */
    public void validateBusinessAccess(Long businessId, Long requestingBusinessId) {
        if (!businessId.equals(requestingBusinessId)) {
            log.warn("Unauthorized access attempt: Business {} tried to access Business {}", 
                requestingBusinessId, businessId);
            throw new SubscribeException("Access denied: Cannot access data from another business");
        }
    }

    /**
     * Checks if a business exists and is active
     */
    public boolean isBusinessActiveById(Long businessId) {
        return businessRepository.findByIdAndActive(businessId).isPresent();
    }

    /**
     * Retrieves business by email for authentication purposes
     */
    @Transactional(readOnly = true)
    public Business getBusinessByEmail(String email) {
        return businessRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found with email: " + email));
    }

    /**
     * Converts Business entity to BusinessDTO
     */
    private BusinessDTO convertToDTO(Business business) {
        BusinessDTO dto = new BusinessDTO();
        dto.setId(business.getId());
        dto.setName(business.getName());
        dto.setEmail(business.getEmail());
        dto.setPhoneNumber(business.getPhoneNumber());
        dto.setActive(business.getActive());
        dto.setTimezone(business.getTimezone());
        dto.setCurrency(business.getCurrency());
        dto.setCreatedAt(business.getCreatedAt());
        dto.setUpdatedAt(business.getUpdatedAt());
        return dto;
    }
}