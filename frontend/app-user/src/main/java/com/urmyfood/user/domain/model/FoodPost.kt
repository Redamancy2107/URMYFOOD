package com.urmyfood.user.domain.model

data class FoodPost(
    val postId: String,
    val dishName: String,
    val price: Double,
    val originalPrice: Double,
    val maxQuantity: Int,
    val remainingQuantity: Int,
    val endTime: String?,
    val isFlashSale: Boolean,
    val status: String,
    val content: String?,
    val imageUrl: String?,
    val shopAccountId: Long = 0L,
    val shopName: String,
    val shopAvatarUrl: String?,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isFollowingShop: Boolean = false,
    val isSaved: Boolean = false,
    val commentCount: Int = 0,
    val category: String? = null,
    val shopAddress: String? = null,
    val createdAt: String? = null
)
