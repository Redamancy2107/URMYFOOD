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
public class OrderItemResponse {
    @JsonProperty("order_item_id")
    private UUID orderItemId;

    @JsonProperty("post_id")
    private UUID postId;

    @JsonProperty("dish_name")
    private String dishName;

    @JsonProperty("image_url")
    private String imageUrl;

    private int quantity;

    @JsonProperty("price_at_purchase")
    private BigDecimal priceAtPurchase;

    private BigDecimal subtotal;
}
