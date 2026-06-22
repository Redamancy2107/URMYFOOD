package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    @NotBlank(message = "Lý do hủy đơn không được để trống")
    @JsonAlias("cancel_reason")
    private String cancelReason;
}
