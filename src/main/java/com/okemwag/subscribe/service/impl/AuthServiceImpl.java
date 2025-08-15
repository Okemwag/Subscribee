package com.okemwag.subscribe.service.impl;

import com.okemwag.subscribe.dto.*;
import com.okemwag.subscribe.entity.Business;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.exception.SubscribeException;
import com.okemwag.subscribe.repository.BusinessRepository;
import com.okemwag.subscribe.security.JwtTokenProvider;
import com.okemwag.subscribe.security.UserPrincipal;
import com.okemwag.subscribe.service.interfaces.AuthService;
import com.okemwag.subscribe.service.interfaces.BusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final BusinessRepository businessRepository;
  private final JwtTokenProvider tokenProvider;
  private final ModelMapper modelMapper;
  private final BusinessService businessService;

  @Override
  @Transactional
  public LoginResponseDTO login(LoginRequestDTO loginRequest) {
    try {
      // Authenticate user
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.getEmail(), loginRequest.getPassword()));

      // Generate JWT token
      String accessToken = tokenProvider.generateToken(authentication);

      // Get user details
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      BusinessDTO businessDTO =
          modelMapper.map(
              businessRepository
                  .findById(userPrincipal.getId())
                  .orElseThrow(() -> new ResourceNotFoundException("Business not found")),
              BusinessDTO.class);

      // For now, using the same token as refresh token (should be implemented separately)
      String refreshToken =
          tokenProvider.generateTokenFromBusinessId(
              userPrincipal.getId(), userPrincipal.getEmail(), userPrincipal.getBusinessName());

      log.info("Business {} logged in successfully", loginRequest.getEmail());

      return new LoginResponseDTO(
          accessToken,
          refreshToken,
          86400L, // 24 hours in seconds
          businessDTO);

    } catch (Exception e) {
      log.error("Login failed for email: {}", loginRequest.getEmail(), e);
      throw new SubscribeException("Invalid email or password");
    }
  }

  @Override
  @Transactional
  public LoginResponseDTO register(BusinessRegistrationDTO registrationRequest) {
    // Check if business already exists
    if (businessRepository.existsByEmail(registrationRequest.getEmail())) {
      throw new SubscribeException("Business with this email already exists");
    }

    // Create new business
    BusinessDTO businessDTO = businessService.registerBusiness(registrationRequest);

    // Generate tokens
    String accessToken =
        tokenProvider.generateTokenFromBusinessId(
            businessDTO.getId(), businessDTO.getEmail(), businessDTO.getName());

    String refreshToken =
        tokenProvider.generateTokenFromBusinessId(
            businessDTO.getId(), businessDTO.getEmail(), businessDTO.getName());

    log.info("New business registered: {}", registrationRequest.getEmail());

    return new LoginResponseDTO(
        accessToken,
        refreshToken,
        86400L, // 24 hours in seconds
        businessDTO);
  }

  @Override
  public LoginResponseDTO refreshToken(String refreshToken) {
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new SubscribeException("Invalid refresh token");
    }

    Long businessId = tokenProvider.getBusinessIdFromToken(refreshToken);
    Business business =
        businessRepository
            .findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

    if (!business.getActive()) {
      throw new SubscribeException("Business account is inactive");
    }

    // Generate new access token
    String newAccessToken =
        tokenProvider.generateTokenFromBusinessId(
            business.getId(), business.getEmail(), business.getName());

    BusinessDTO businessDTO = modelMapper.map(business, BusinessDTO.class);

    return new LoginResponseDTO(
        newAccessToken,
        refreshToken, // Keep the same refresh token
        86400L,
        businessDTO);
  }

  @Override
  public void logout(String token) {
    // For now, we'll just log the logout
    // In a production system, you might want to blacklist the token
    log.info("User logged out");
  }

  @Override
  public boolean validateToken(String token) {
    return tokenProvider.validateToken(token);
  }

  @Override
  public BusinessDTO getCurrentBusiness() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new SubscribeException("No authenticated user found");
    }

    Long businessId = (Long) authentication.getPrincipal();
    Business business =
        businessRepository
            .findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

    return modelMapper.map(business, BusinessDTO.class);
  }
}
