package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

data class SavedPostResponse(
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("is_saved")
    val isSaved: Boolean
)
