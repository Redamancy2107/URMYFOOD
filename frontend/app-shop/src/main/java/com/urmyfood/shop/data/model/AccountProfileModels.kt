package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shop.domain.model.UserProfile

data class UpdateProfileRequest(
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class AccountProfileResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("role")
    val role: String,
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)

fun AccountProfileResponse.toDomain() = UserProfile(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    role = role,
    avatarUrl = avatarUrl
)
