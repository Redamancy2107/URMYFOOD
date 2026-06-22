package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("post_id") val postId: String,
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("price") val price: Double,
    @SerializedName("original_price") val originalPrice: Double,
    @SerializedName("max_quantity") val maxQuantity: Int,
    @SerializedName("remaining_quantity") val remainingQuantity: Int,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("content") val content: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("shop_account_id") val shopAccountId: Long = 0L,
    @SerializedName("shop_name") val shopName: String,
    @SerializedName("shop_avatar_url") val shopAvatarUrl: String?,
    @SerializedName("shop_address") val shopAddress: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("is_liked") val isLiked: Boolean = false,
    @SerializedName("is_following_shop") val isFollowingShop: Boolean = false,
    @SerializedName("is_saved") val isSaved: Boolean = false,
    @SerializedName("comment_count") val commentCount: Int = 0
)
