package com.urmyfood.shop.domain.model

data class Post(
    val postId: String,
    val dishName: String,
    val price: Long,
    val originalPrice: Long?,
    val maxQuantity: Int,
    val remainingQuantity: Int,
    val endTime: String?,
    val isFlashSale: Boolean,
    val status: String,
    val content: String?,
    val imageUrl: String?,
    val shopName: String,
    val shopAvatarUrl: String?,
    val createdAt: String,
    val likeCount: Long,
    val isLiked: Boolean,
    val commentCount: Long,
    val category: String?
) {
    val isActive: Boolean get() = status == "ACTIVE"
    val stock: Int get() = remainingQuantity
    val maxStock: Int get() = maxQuantity
}
