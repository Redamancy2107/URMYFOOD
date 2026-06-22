package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Shop verification request from /api/v1/admin/verifications/pending */
data class ShopVerification(
    @SerializedName("id") val id: Long,
    @SerializedName("shop_id") val shopId: Long,
    @SerializedName("shop_name") val shopName: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("cccd_front_url") val cccdFrontUrl: String?,
    @SerializedName("cccd_back_url") val cccdBackUrl: String?,
    @SerializedName("shop_photo_urls") val shopPhotoUrls: List<String>?,
    @SerializedName("status") val status: String?,
    @SerializedName("reject_reason") val rejectReason: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
