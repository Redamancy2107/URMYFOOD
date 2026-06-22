package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Voucher extends BaseDomainModel {
    private String code;
    private String title;
    private String description;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private LocalDate expiryDate;
    private boolean isActive;
}
