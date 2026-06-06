package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName

data class ShopVerificationRequest(
    @SerializedName("shop_name")
    val shopName: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("cccd_front_url")
    val cccdFrontUrl: String,
    @SerializedName("cccd_back_url")
    val cccdBackUrl: String,
    @SerializedName("shop_photo_urls")
    val shopPhotoUrls: List<String>
)

data class ShopVerificationResponse(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("shop_id")
    val shopId: Long?,
    @SerializedName("shop_name")
    val shopName: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("reject_reason")
    val rejectReason: String?
)
