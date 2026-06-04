package com.urmyfood.shop.domain.model

data class ShopRegistrationData(
    val shopName: String,
    val category: ShopCategory,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val cccdFrontUri: String?,
    val cccdBackUri: String?,
    val shopPhotoUris: List<String>
)
