package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewResponse {
    private UUID id;

    @JsonProperty("order_id")
    private UUID orderId;

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("shop_id")
    private Long shopId;

    private int rating;
    private String comment;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
