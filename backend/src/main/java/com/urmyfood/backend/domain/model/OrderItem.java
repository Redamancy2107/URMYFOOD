package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private UUID orderItemId;
    private UUID orderId;
    private Post post;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private String dishNameSnapshot;
    private String imageUrlSnapshot;
}
