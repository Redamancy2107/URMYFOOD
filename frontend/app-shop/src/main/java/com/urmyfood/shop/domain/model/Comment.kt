package com.urmyfood.shop.domain.model

data class Comment(
    val commentId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val content: String,
    val createdAt: String,
    val parentId: String? = null
)
