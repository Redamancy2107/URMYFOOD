package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shop.domain.model.Comment
import com.urmyfood.shop.domain.model.Post

data class PostDto(
    @SerializedName("post_id") val postId: String?,
    @SerializedName("dish_name") val dishName: String?,
    @SerializedName("price") val price: Long?,
    @SerializedName("original_price") val originalPrice: Long?,
    @SerializedName("max_quantity") val maxQuantity: Int?,
    @SerializedName("remaining_quantity") val remainingQuantity: Int?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean?,
    @SerializedName("status") val status: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("shop_name") val shopName: String?,
    @SerializedName("shop_avatar_url") val shopAvatarUrl: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("like_count") val likeCount: Long?,
    @SerializedName("is_liked") val isLiked: Boolean?,
    @SerializedName("comment_count") val commentCount: Long?,
    @SerializedName("category") val category: String?
)

fun PostDto.toDomain() = Post(
    postId = postId.orEmpty(),
    dishName = dishName.orEmpty(),
    price = price ?: 0L,
    originalPrice = originalPrice,
    maxQuantity = maxQuantity ?: 0,
    remainingQuantity = remainingQuantity ?: 0,
    endTime = endTime,
    isFlashSale = isFlashSale ?: false,
    status = status.orEmpty(),
    content = content,
    imageUrl = imageUrl,
    shopName = shopName.orEmpty(),
    shopAvatarUrl = shopAvatarUrl,
    createdAt = createdAt.orEmpty(),
    likeCount = likeCount ?: 0L,
    isLiked = isLiked ?: false,
    commentCount = commentCount ?: 0L,
    category = category
)

data class PostPageDto(
    @SerializedName("content") val content: List<PostDto>?,
    @SerializedName("page") val page: Int?,
    @SerializedName("size") val size: Int?,
    @SerializedName("total_elements") val totalElements: Long?,
    @SerializedName("has_next") val hasNext: Boolean?,
    @SerializedName("anchor") val anchor: String?
)

data class CreatePostRequest(
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("price") val price: Long,
    @SerializedName("original_price") val originalPrice: Long?,
    @SerializedName("max_quantity") val maxQuantity: Int,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean,
    @SerializedName("content") val content: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("category") val category: String?
)

data class UpdatePostRequest(
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("price") val price: Long,
    @SerializedName("original_price") val originalPrice: Long?,
    @SerializedName("max_quantity") val maxQuantity: Int,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean,
    @SerializedName("content") val content: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("category") val category: String?
)

data class UpdatePostStatusRequest(
    @SerializedName("status") val status: String
)

data class PostImageUploadResponse(
    @SerializedName("image_url") val imageUrl: String?
)

data class CommentDto(
    @SerializedName("comment_id") val commentId: String?,
    @SerializedName("author_name") val authorName: String?,
    @SerializedName("author_avatar_url") val authorAvatarUrl: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("parent_id") val parentId: String?
)

fun CommentDto.toDomain() = Comment(
    commentId = commentId.orEmpty(),
    authorName = authorName.orEmpty(),
    authorAvatarUrl = authorAvatarUrl,
    content = content.orEmpty(),
    createdAt = createdAt.orEmpty(),
    parentId = parentId
)

data class CommentPageDto(
    @SerializedName("content") val content: List<CommentDto>?,
    @SerializedName("has_next") val hasNext: Boolean?,
    @SerializedName("next_cursor") val nextCursor: String?
)

data class AddCommentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("parent_id") val parentId: String?
)
