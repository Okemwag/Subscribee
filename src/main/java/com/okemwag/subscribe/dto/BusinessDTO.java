package com.okemwag.subscribe.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BusinessDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean active;
    private String timezone;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}