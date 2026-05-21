package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.CreateCommentRequest
import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.data.remote.PostApiService
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository
import kotlinx.coroutines.CancellationException

class PostRepositoryImpl(
    private val postApiService: PostApiService
) : PostRepository {

    override suspend fun getPosts(token: String?): Result<List<FoodPost>> {
        return try {
            val response = postApiService.getPosts(token)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.map { it.toDomain() })
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

    override suspend fun getComments(postId: String): Result<List<Comment>> {
        return try {
            val response = postApiService.getComments(postId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.map { it.toDomain() })
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

    override suspend fun postComment(postId: String, content: String, token: String): Result<Comment> {
        return try {
            val response = postApiService.postComment(postId, token, CreateCommentRequest(content))
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
}
