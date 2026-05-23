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
    shopName = shopName,
    shopAvatarUrl = shopAvatarUrl,
    likeCount = likeCount,
    isLiked = isLiked,
    commentCount = commentCount
)
