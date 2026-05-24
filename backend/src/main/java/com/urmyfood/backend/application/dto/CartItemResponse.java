package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    @JsonProperty("cart_item_id")
    private UUID cartItemId;

    @JsonProperty("post_id")
    private UUID postId;

    @JsonProperty("dish_name")
    private String dishName;

    private BigDecimal price;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    private int quantity;

    private BigDecimal subtotal;

    @JsonProperty("remaining_quantity")
    private int remainingQuantity;
}
