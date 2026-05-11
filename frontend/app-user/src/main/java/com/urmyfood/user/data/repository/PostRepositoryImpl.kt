package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.data.remote.PostApiService
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository
import kotlinx.coroutines.CancellationException

class PostRepositoryImpl(
    private val postApiService: PostApiService
) : PostRepository {

    override suspend fun getPosts(): Result<List<FoodPost>> {
        return try {
            val response = postApiService.getPosts()
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
}
