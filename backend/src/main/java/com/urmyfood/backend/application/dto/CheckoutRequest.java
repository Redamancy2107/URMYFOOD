package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @JsonAlias("payment_method")
    private String paymentMethod;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @JsonAlias("delivery_address")
    private String deliveryAddress;

    @JsonAlias("voucher_id")
    private Long voucherId;

    private String note;
}
