package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

    @JsonProperty("post_id")
    private UUID postId;

    @JsonProperty("dish_name")
    private String dishName;

    private BigDecimal price;

    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @JsonProperty("max_quantity")
    private int maxQuantity;

    @JsonProperty("remaining_quantity")
    private int remainingQuantity;

    @JsonProperty("end_time")
    private OffsetDateTime endTime;

    @JsonProperty("is_flash_sale")
    private boolean isFlashSale;

    private String status;

    private String content;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("shop_avatar_url")
    private String shopAvatarUrl;

    @JsonProperty("shop_address")
    private String shopAddress;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("is_liked")
    private boolean isLiked;

    @JsonProperty("comment_count")
    private long commentCount;

    @JsonProperty("category")
    private String category;
}
