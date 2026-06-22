package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

data class AdminProfile(
    @SerializedName("id") val id: Long?,
    @SerializedName("account_id") val accountId: Long?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("work_email") val workEmail: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("position") val position: String?,
    @SerializedName("short_bio") val shortBio: String?,
    @SerializedName("is_2fa_enabled") val is2FaEnabled: Boolean = false,
    @SerializedName("avatar_url") val avatarUrl: String?
)
