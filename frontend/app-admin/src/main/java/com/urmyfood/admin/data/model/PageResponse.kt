package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Generic paginated response matching backend PageResponse<T> */
data class PageResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total_elements") val totalElements: Long,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean
)
