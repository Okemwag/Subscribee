package com.okemwag.subscribe.service.interfaces;

import com.okemwag.subscribe.dto.BusinessRegistrationDTO;
import com.okemwag.subscribe.dto.LoginRequestDTO;
import com.okemwag.subscribe.dto.LoginResponseDTO;
import com.okemwag.subscribe.dto.BusinessDTO;

public interface AuthService {
    
    /**
     * Authenticate a business user and generate JWT token
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * Register a new business and return authentication token
     */
    LoginResponseDTO register(BusinessRegistrationDTO registrationRequest);
    
    /**
     * Refresh JWT token
     */
    LoginResponseDTO refreshToken(String refreshToken);
    
    /**
     * Logout user (invalidate token)
     */
    void logout(String token);
    
    /**
     * Validate if token is valid and not blacklisted
     */
    boolean validateToken(String token);
    
    /**
     * Get current authenticated business
     */
    BusinessDTO getCurrentBusiness();
}