package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {
    @NotNull(message = "postId không được để trống")
    @JsonAlias("post_id")
    private UUID postId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
}
