package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Post response from /api/v1/admin/posts */
data class PostItem(
    @SerializedName("post_id") val postId: String,
    @SerializedName("dish_name") val dishName: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("original_price") val originalPrice: Double?,
    @SerializedName("max_quantity") val maxQuantity: Int = 0,
    @SerializedName("remaining_quantity") val remainingQuantity: Int = 0,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean = false,
    @SerializedName("status") val status: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("shop_account_id") val shopAccountId: Long?,
    @SerializedName("shop_name") val shopName: String?,
    @SerializedName("shop_avatar_url") val shopAvatarUrl: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("created_at") val createdAt: String?
)
