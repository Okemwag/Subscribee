package com.okemwag.subscribe.config;

import com.okemwag.subscribe.security.JwtAuthenticationEntryPoint;
import com.okemwag.subscribe.security.JwtAuthenticationFilter;
import com.okemwag.subscribe.security.TenantIsolationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final TenantIsolationFilter tenantIsolationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .authorizeHttpRequests(
            authz ->
                authz
                    // Public endpoints
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/actuator/health")
                    .permitAll()

                    // Business owner endpoints
                    .requestMatchers("/api/v1/businesses/**")
                    .hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/v1/customers/**")
                    .hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/v1/subscription-plans/**")
                    .hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/v1/subscriptions/**")
                    .hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/v1/payments/**")
                    .hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/v1/invoices/**")
                    .hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/v1/analytics/**")
                    .hasRole("BUSINESS_OWNER")

                    // All other requests require authentication
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(tenantIsolationFilter, JwtAuthenticationFilter.class);

    return http.build();
  }
}
