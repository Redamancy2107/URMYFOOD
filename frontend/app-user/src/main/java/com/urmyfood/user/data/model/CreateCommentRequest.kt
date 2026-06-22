package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

data class CreateCommentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("parent_id") val parentId: String? = null
)
