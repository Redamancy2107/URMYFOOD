package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    private UUID postId;
    private String dishName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int maxQuantity;
    private int remainingQuantity;
    private OffsetDateTime endTime;
    private boolean isFlashSale;
    private PostStatus status;
    private String content;
    private String imageUrl;
    private String category;
    private Account author;
    private OffsetDateTime createdAt;
}
