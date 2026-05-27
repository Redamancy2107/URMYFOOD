package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.*
import com.urmyfood.user.data.remote.PostApiService
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository
import kotlinx.coroutines.CancellationException

class PostRepositoryImpl(
    private val postApiService: PostApiService
) : PostRepository {

    override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?): Result<PageResult<FoodPost>> {
        return try {
            val response = postApiService.getPosts(token, page, size, anchor)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val pageData = body.data
                    Result.Success(PageResult(
                        items = pageData.content.map { it.toDomain() },
                        page = pageData.page,
                        hasNext = pageData.hasNext,
                        nextCursor = pageData.nextCursor,
                        anchor = pageData.anchor
                    ))
                } else {
                    Result.Error(body?.message ?: "Không thể tải danh sách bài viết")
                }
            } else {
                Result.Error("Lỗi server: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Không thể kết nối đến server")
        }
    }

    override suspend fun searchPosts(token: String?, query: String, page: Int, size: Int, anchor: String?): Result<PageResult<FoodPost>> {
        return try {
            val response = postApiService.searchPosts(token, query, page, size, anchor)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val pageData = body.data
                    Result.Success(PageResult(
                        items = pageData.content.map { it.toDomain() },
                        page = pageData.page,
                        hasNext = pageData.hasNext,
                        nextCursor = pageData.nextCursor,
                        anchor = pageData.anchor
                    ))
                } else {
                    Result.Error(body?.message ?: "Không thể tìm kiếm bài viết")
                }
            } else {
                Result.Error("Lỗi server: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Không thể kết nối đến server")
        }
    }

    override suspend fun toggleLike(
        postId: String,
        isCurrentlyLiked: Boolean,
        token: String
    ): Result<LikeToggleResult> {
        return try {
            val response = if (isCurrentlyLiked) {
                postApiService.unlikePost(postId, token)
            } else {
                postApiService.likePost(postId, token)
            }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(LikeToggleResult(body.data.likeCount, body.data.isLiked))
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật lượt thích")
                }
            } else {
                Result.Error("Lỗi server: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Không thể kết nối đến server")
        }
    }

    override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int): Result<PageResult<Comment>> {
        return try {
            val response = postApiService.getComments(postId, token, cursor, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val pageData = body.data
                    Result.Success(PageResult(
                        items = pageData.content.map { it.toDomain() },
                        page = pageData.page,
                        hasNext = pageData.hasNext,
                        nextCursor = pageData.nextCursor,
                        anchor = pageData.anchor
                    ))
                } else {
                    Result.Error(body?.message ?: "Không thể tải bình luận")
                }
            } else {
                Result.Error("Lỗi server: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Không thể kết nối đến server")
        }
    }

    override suspend fun postComment(postId: String, content: String, token: String, parentId: String?): Result<Comment> {
        return try {
            val response = postApiService.postComment(postId, token, CreateCommentRequest(content, parentId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể đăng bình luận")
                }
            } else {
                Result.Error("Lỗi server: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Không thể kết nối đến server")
        }
    }

    override suspend fun getPost(postId: String, token: String?): Result<FoodPost> {
        return try {
            val response = postApiService.getPost(postId, token)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể tải bài viết")
                }
            } else {
                Result.Error("Lỗi server: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Không thể kết nối đến server")
        }
    }
}
