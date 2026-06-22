package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName

data class PageResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total_elements") val totalElements: Long,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("next_cursor") val nextCursor: String? = null,
    @SerializedName("anchor") val anchor: String? = null
)
