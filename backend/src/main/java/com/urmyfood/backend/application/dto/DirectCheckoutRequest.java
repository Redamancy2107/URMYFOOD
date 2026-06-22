package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectCheckoutRequest {
    @NotNull(message = "postId khong duoc de trong")
    @JsonAlias("post_id")
    private UUID postId;

    @Min(value = 1, message = "So luong phai lon hon 0")
    private int quantity;

    @NotBlank(message = "Phuong thuc thanh toan khong duoc de trong")
    @JsonAlias("payment_method")
    private String paymentMethod;

    @NotBlank(message = "Dia chi giao hang khong duoc de trong")
    @JsonAlias("delivery_address")
    private String deliveryAddress;

    @JsonAlias("voucher_id")
    private Long voucherId;

    @JsonAlias("voucher_code")
    private String voucherCode;

    private String note;
}
