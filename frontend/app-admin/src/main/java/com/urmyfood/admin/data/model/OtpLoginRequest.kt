package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

data class OtpLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)
