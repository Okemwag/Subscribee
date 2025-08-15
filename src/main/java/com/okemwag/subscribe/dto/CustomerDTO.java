package com.okemwag.subscribe.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean active;
    private String preferredLanguage;
    private Long businessId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}