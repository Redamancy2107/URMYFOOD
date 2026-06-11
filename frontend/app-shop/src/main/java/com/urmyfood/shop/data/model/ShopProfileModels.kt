package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shop.domain.model.ShopProfile

data class ShopProfileRequest(
    @SerializedName("shop_name")
    val shopName: String,
    @SerializedName("logo_url")
    val logoUrl: String?,
    @SerializedName("cover_url")
    val coverUrl: String?,
    @SerializedName("category")
    val category: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("opening_hours")
    val openingHours: String,
    @SerializedName("is_open")
    val isOpen: Boolean
)

data class ShopProfileResponse(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("shop_id")
    val shopId: Long?,
    @SerializedName("shop_name")
    val shopName: String?,
    @SerializedName("logo_url")
    val logoUrl: String?,
    @SerializedName("cover_url")
    val coverUrl: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("opening_hours")
    val openingHours: String?,
    @SerializedName("is_open")
    val isOpen: Boolean?,
    @SerializedName("verification_status")
    val verificationStatus: String?
)

fun ShopProfileResponse.toDomain() = ShopProfile(
    id = id,
    shopId = shopId,
    shopName = shopName.orEmpty(),
    logoUrl = logoUrl,
    coverUrl = coverUrl,
    category = category.orEmpty(),
    address = address.orEmpty(),
    latitude = latitude,
    longitude = longitude,
    description = description,
    openingHours = openingHours.orEmpty(),
    isOpen = isOpen ?: true,
    verificationStatus = verificationStatus ?: "NOT_SUBMITTED"
)

fun ShopProfile.toRequest() = ShopProfileRequest(
    shopName = shopName,
    logoUrl = logoUrl,
    coverUrl = coverUrl,
    category = category,
    address = address,
    latitude = latitude,
    longitude = longitude,
    description = description,
    openingHours = openingHours,
    isOpen = isOpen
)
