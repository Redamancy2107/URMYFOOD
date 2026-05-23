package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.user.domain.model.Comment

data class CommentResponse(
    @SerializedName("comment_id") val commentId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_avatar_url") val authorAvatarUrl: String?,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
)

fun CommentResponse.toDomain() = Comment(
    commentId = commentId,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    content = content,
    createdAt = createdAt
)
