package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("accountId") val accountId: Long?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("role") val role: String?
)
