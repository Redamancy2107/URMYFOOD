package com.urmyfood.shared.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("emailOrPhone")
    val emailOrPhone: String,
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("otpCode")
    val otpCode: String
)

data class SendOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String?
)

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp_code")
    val otpCode: String
)

data class ResetPasswordRequest(
    @SerializedName("reset_token")
    val resetToken: String,
    @SerializedName("new_password")
    val newPassword: String
)
