package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response DTO cho địa chỉ từ API.
 */
data class AddressResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("label")
    val label: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("detail")
    val detail: String,
    @SerializedName("default")
    val isDefault: Boolean
)

/**
 * Request body cho tạo / cập nhật địa chỉ.
 */
data class AddressRequest(
    @SerializedName("label")
    val label: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("detail")
    val detail: String,
    @SerializedName("default")
    val isDefault: Boolean
)
