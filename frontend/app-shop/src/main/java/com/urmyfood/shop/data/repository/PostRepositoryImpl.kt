package com.urmyfood.shop.data.repository

import com.google.gson.Gson
import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.AddCommentRequest
import com.urmyfood.shop.data.model.CreatePostRequest
import com.urmyfood.shop.data.model.UpdatePostRequest
import com.urmyfood.shop.data.model.UpdatePostStatusRequest
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.data.remote.PostApiService
import com.urmyfood.shop.data.util.toUserMessage
import com.urmyfood.shop.domain.model.Comment
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.repository.PostRepository
import okhttp3.MultipartBody

class PostRepositoryImpl(
    private val apiService: PostApiService
) : PostRepository {

    private val gson = Gson()

    override suspend fun getMyPosts(token: String, page: Int, size: Int): Result<List<Post>> {
        return try {
            val response = apiService.getMyPosts(token, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.content?.map { it.toDomain() } ?: emptyList())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy danh sách bài viết")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun getPostById(token: String, postId: String): Result<Post> {
        return try {
            val response = apiService.getPostById(token, postId)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy bài viết")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun createPost(token: String, request: CreatePostRequest): Result<Post> {
        return try {
            val response = apiService.createPost(token, request)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể tạo bài viết")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun updatePost(token: String, postId: String, request: UpdatePostRequest): Result<Post> {
        return try {
            val response = apiService.updatePost(token, postId, request)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật bài viết")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun updateRemainingQuantity(token: String, postId: String, remainingQuantity: Int): Result<Post> {
        return try {
            val response = apiService.updateRemainingQuantity(
                token, postId,
                com.urmyfood.shop.data.model.UpdateRemainingQuantityRequest(remainingQuantity)
            )
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật số suất còn lại")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun deletePost(token: String, postId: String): Result<Unit> {
        return try {
            val response = apiService.deletePost(token, postId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.Success(Unit)
                } else {
                    Result.Error(body?.message ?: "Không thể xóa bài viết")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun updatePostStatus(token: String, postId: String, status: String): Result<Post> {
        return try {
            val response = apiService.updatePostStatus(token, postId, UpdatePostStatusRequest(status))
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật trạng thái")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun uploadPostImage(token: String, file: MultipartBody.Part): Result<String> {
        return try {
            val response = apiService.uploadPostImage(token, file)
            if (response.isSuccessful) {
                val body = response.body()
                val imageUrl = body?.data?.imageUrl
                if (body?.success == true && !imageUrl.isNullOrBlank()) {
                    Result.Success(imageUrl)
                } else {
                    Result.Error(body?.message ?: "Không thể tải ảnh lên")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun getComments(token: String, postId: String): Result<List<Comment>> {
        return try {
            val response = apiService.getComments(token, postId)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.content?.map { it.toDomain() } ?: emptyList())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy bình luận")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun addComment(token: String, postId: String, content: String, parentId: String?): Result<Comment> {
        return try {
            val response = apiService.addComment(token, postId, AddCommentRequest(content, parentId))
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể đăng bình luận")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    private fun parseErrorMessage(errorBodyStr: String?): String? {
        return try {
            gson.fromJson(errorBodyStr, ApiResponse::class.java)?.message
        } catch (e: Exception) {
            null
        }
    }
}
