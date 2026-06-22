package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Account profile from /api/v1/admin/accounts */
data class AccountProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String?,
    @SerializedName(value = "avatarUrl", alternate = ["avatar_url"]) val avatarUrl: String?,
    @SerializedName("active") val isActive: Boolean = true
)
