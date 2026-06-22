package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRemainingQuantityRequest {

    @Min(value = 0, message = "Số suất còn lại không được âm")
    @JsonProperty("remaining_quantity")
    private int remainingQuantity;
}
