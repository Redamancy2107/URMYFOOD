package com.urmyfood.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long id;
    private String label;
    private String name;
    private String phone;
    private String detail;
    
    @JsonProperty("default")
    private boolean isDefault;
}
