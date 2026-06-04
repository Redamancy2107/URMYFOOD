package com.urmyfood.shared.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shared.domain.model.AuthToken

data class LoginResponse(
    @SerializedName("token")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    @SerializedName("expiresIn")
    val expiresIn: Long?,
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("role")
    val role: String?
)

data class VerifyOtpResponse(
    @SerializedName("reset_token")
    val resetToken: String
)

fun LoginResponse.toDomain() = AuthToken(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    fullName = fullName,
    role = role
)
