package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName

data class ProfileImageUploadResponse(
    @SerializedName("image_url")
    val imageUrl: String?
)
