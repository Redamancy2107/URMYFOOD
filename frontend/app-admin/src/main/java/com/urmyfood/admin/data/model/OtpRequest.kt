package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null
)
