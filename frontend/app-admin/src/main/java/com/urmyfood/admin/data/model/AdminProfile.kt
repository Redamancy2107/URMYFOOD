package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

data class AdminProfile(
    @SerializedName("id") val id: Long?,
    @SerializedName("accountId") val accountId: Long?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("workEmail") val workEmail: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("position") val position: String?,
    @SerializedName("shortBio") val shortBio: String?,
    @SerializedName("is2FaEnabled") val is2FaEnabled: Boolean = false,
    @SerializedName("avatarUrl") val avatarUrl: String?
)
