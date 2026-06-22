package com.urmyfood.user.data.model

import com.urmyfood.user.domain.model.FoodPost

fun PostResponse.toDomain() = FoodPost(
    postId = postId,
    dishName = dishName,
    price = price,
    originalPrice = originalPrice,
    maxQuantity = maxQuantity,
    remainingQuantity = remainingQuantity,
    endTime = endTime,
    isFlashSale = isFlashSale,
    status = status,
    content = content,
    imageUrl = imageUrl,
    shopAccountId = shopAccountId,
    shopName = shopName,
    shopAvatarUrl = shopAvatarUrl,
    likeCount = likeCount,
    isLiked = isLiked,
    isFollowingShop = isFollowingShop,
    commentCount = commentCount,
    shopAddress = shopAddress,
    createdAt = createdAt
)
