package com.urmyfood.shop.domain.model

data class ShopProfile(
    val id: Long?,
    val shopId: Long?,
    val shopName: String,
    val logoUrl: String?,
    val coverUrl: String?,
    val category: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val description: String?,
    val openingHours: String,
    val isOpen: Boolean,
    val verificationStatus: String
)
