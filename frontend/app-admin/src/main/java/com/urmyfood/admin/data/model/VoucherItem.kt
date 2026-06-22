package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Voucher from /api/v1/admin/vouchers */
data class VoucherItem(
    @SerializedName("id") val id: Long?,
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("discountValue") val discountValue: Double?,
    @SerializedName("minOrderValue") val minOrderValue: Double?,
    @SerializedName("expiryDate") val expiryDate: String?
)
