package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp extends BaseDomainModel {
    private String email;
    private String code;
    private LocalDateTime expiryTime;
    private boolean used;
}
