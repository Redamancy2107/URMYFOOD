package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response DTO cho voucher từ API.
 */
data class VoucherResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("code")
    val code: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("discountValue")
    val discountValue: Double,
    @SerializedName("minOrderValue")
    val minOrderValue: Double,
    @SerializedName("expiryDate")
    val expiryDate: String,
    @SerializedName("is_saved")
    val isSaved: Boolean = false
)

data class SavedVoucherResponse(
    @SerializedName("voucher_id")
    val voucherId: Long,
    @SerializedName("is_saved")
    val isSaved: Boolean
)
