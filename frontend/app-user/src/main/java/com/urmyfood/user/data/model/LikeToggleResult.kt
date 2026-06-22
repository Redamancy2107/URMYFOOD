package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

data class LikeToggleResult(
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("is_liked") val isLiked: Boolean
)
