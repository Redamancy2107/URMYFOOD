package com.urmyfood.backend.application.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
}
