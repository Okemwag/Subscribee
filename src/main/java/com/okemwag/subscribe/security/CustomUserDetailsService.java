package com.okemwag.subscribe.security;

import com.okemwag.subscribe.entity.Business;
import com.okemwag.subscribe.exception.ResourceNotFoundException;
import com.okemwag.subscribe.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final BusinessRepository businessRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Business not found with email: " + email));

        return UserPrincipal.create(business);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + id));

        return UserPrincipal.create(business);
    }
}