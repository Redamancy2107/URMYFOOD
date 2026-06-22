package com.urmyfood.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Long id;
    private String code;
    private String title;
    private String description;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private LocalDate expiryDate;

    @JsonProperty("is_saved")
    private boolean isSaved;
}
