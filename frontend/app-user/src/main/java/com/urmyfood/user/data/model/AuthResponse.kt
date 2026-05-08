package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper.
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: T?
)

/**
 * Response DTO for login (contains tokens).
 */
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long
)

/**
 * Response DTO for user data.
 */
data class UserResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @SerializedName("status")
    val status: String
)

/**
 * Response DTO for OTP verification (contains reset token).
 */
data class VerifyOtpResponse(
    @SerializedName("reset_token")
    val resetToken: String
)
