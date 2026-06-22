package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    private UUID id;
    private String email;
    private String code;
    private LocalDateTime expiryTime;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
