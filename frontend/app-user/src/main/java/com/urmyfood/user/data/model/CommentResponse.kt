package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.user.domain.model.Comment
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CommentResponse(
    @SerializedName("comment_id") val commentId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_avatar_url") val authorAvatarUrl: String?,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
)

private val commentDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))

fun CommentResponse.toDomain() = Comment(
    commentId = commentId,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    content = content,
    createdAt = formatCommentTime(createdAt)
)

private fun formatCommentTime(value: String): String =
    runCatching {
        OffsetDateTime.parse(value).format(commentDisplayFormatter)
    }.getOrElse { value }
