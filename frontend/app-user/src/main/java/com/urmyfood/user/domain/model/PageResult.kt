package com.urmyfood.user.domain.model

data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val hasNext: Boolean,
    val nextCursor: String? = null,
    val anchor: String? = null
)
