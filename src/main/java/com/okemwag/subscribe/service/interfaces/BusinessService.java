package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.BusinessDTO;
import com.okemwag.subscribe.dto.BusinessRegistrationDTO;
import com.okemwag.subscribe.dto.BusinessUpdateDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BusinessService {
    BusinessDTO registerBusiness(BusinessRegistrationDTO dto);
    BusinessDTO updateBusiness(Long businessId, BusinessUpdateDTO dto);
    BusinessDTO getBusinessById(Long businessId);
    void deleteBusiness(Long businessId);
    List<BusinessDTO> getAllBusinesses(Pageable pageable);
}