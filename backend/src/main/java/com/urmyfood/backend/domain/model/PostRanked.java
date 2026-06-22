package com.urmyfood.backend.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PostRanked(
        UUID postId,
        String dishName,
        BigDecimal price,
        BigDecimal originalPrice,
        int maxQuantity,
        int remainingQuantity,
        OffsetDateTime endTime,
        boolean flashSale,
        PostStatus status,
        String content,
        String imageUrl,
        Long shopAccountId,
        String shopName,
        String shopAvatarUrl,
        String shopAddress,
        long likeCount,
        long commentCount,
        boolean liked,
        OffsetDateTime createdAt,
        String category
) {}
