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
public class CreateAddressRequest {
    private String label;
    private String name;
    private String phone;
    private String detail;
    
    @JsonProperty("default")
    private Boolean isDefault;

    public boolean isDefault() {
        return isDefault != null && isDefault;
    }
}
