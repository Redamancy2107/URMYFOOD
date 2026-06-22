package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.PageResponse
import com.urmyfood.user.data.model.PostResponse
import com.urmyfood.user.data.model.SavedPostResponse
import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.data.remote.PostApiService
import com.urmyfood.user.data.util.toUserMessage
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.SavedPostRepository
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class SavedPostRepositoryImpl(
    private val postApiService: PostApiService
) : SavedPostRepository {

    override suspend fun getSavedPosts(token: String, page: Int, size: Int): Result<PageResult<FoodPost>> {
        return try {
            val response = postApiService.getSavedPosts(token, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data.toPageResult())
                } else {
                    Result.Error(body?.message ?: "Khong the tai bai viet da luu")
                }
            } else {
                Result.Error("Loi may chu: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun getSavedState(token: String, postId: String): Result<SavedPostResponse> {
        return savedStateCall { postApiService.getSavedState(postId, token) }
    }

    override suspend fun savePost(token: String, postId: String): Result<SavedPostResponse> {
        return savedStateCall { postApiService.savePost(postId, token) }
    }

    override suspend fun unsavePost(token: String, postId: String): Result<SavedPostResponse> {
        return savedStateCall { postApiService.unsavePost(postId, token) }
    }

    private suspend fun savedStateCall(call: suspend () -> Response<ApiResponse<SavedPostResponse>>): Result<SavedPostResponse> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data)
                } else {
                    Result.Error(body?.message ?: "Khong the cap nhat bai viet da luu")
                }
            } else {
                Result.Error("Loi may chu: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    private fun PageResponse<PostResponse>.toPageResult() = PageResult(
        items = content.map { it.toDomain() },
        page = page,
        hasNext = hasNext,
        nextCursor = nextCursor,
        anchor = anchor
    )
}
