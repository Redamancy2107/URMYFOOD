package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request DTO for login API.
 */
data class LoginRequest(
    @SerializedName("email_or_phone")
    val emailOrPhone: String,
    @SerializedName("password")
    val password: String
)

/**
 * Request DTO for registration API.
 */
data class RegisterRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)

/**
 * Request DTO for forgot password API.
 */
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

/**
 * Request DTO for OTP verification API.
 */
data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp_code")
    val otpCode: String
)

/**
 * Request DTO for password reset API.
 */
data class ResetPasswordRequest(
    @SerializedName("reset_token")
    val resetToken: String,
    @SerializedName("new_password")
    val newPassword: String
)
