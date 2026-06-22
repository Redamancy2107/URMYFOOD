package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

data class ShopProfileResponse(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("shop_id")
    val shopId: Long,
    @SerializedName("shop_name")
    val shopName: String,
    @SerializedName("logo_url")
    val logoUrl: String?,
    @SerializedName("cover_url")
    val coverUrl: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("opening_hours")
    val openingHours: String?,
    @SerializedName("is_open")
    val isOpen: Boolean,
    @SerializedName("verification_status")
    val verificationStatus: String?,
    @SerializedName("is_following")
    val isFollowing: Boolean,
    @SerializedName("follower_count")
    val followerCount: Long
)

data class ShopFollowResponse(
    @SerializedName("shop_id")
    val shopId: Long,
    @SerializedName("is_following")
    val isFollowing: Boolean,
    @SerializedName("follower_count")
    val followerCount: Long
)
