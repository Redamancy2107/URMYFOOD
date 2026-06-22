package com.urmyfood.user.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.ShopFollowResponse
import com.urmyfood.user.data.model.ShopProfileResponse
import com.urmyfood.user.data.remote.ShopApiService
import com.urmyfood.user.data.util.toUserMessage
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.ShopRepository
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class ShopRepositoryImpl(
    private val api: ShopApiService
) : ShopRepository {

    private val gson = Gson()

    override suspend fun getProfile(token: String, shopId: Long): Result<ShopProfileResponse> =
        safeApiCall { api.getProfile(token, shopId) }

    override suspend fun getFollowState(token: String, shopId: Long): Result<ShopFollowResponse> =
        safeApiCall { api.getFollowState(token, shopId) }

    override suspend fun follow(token: String, shopId: Long): Result<ShopFollowResponse> =
        safeApiCall { api.follow(token, shopId) }

    override suspend fun unfollow(token: String, shopId: Long): Result<ShopFollowResponse> =
        safeApiCall { api.unfollow(token, shopId) }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<ApiResponse<T>>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data)
                } else {
                    Result.Error(body?.message ?: "Thao tác thất bại")
                }
            } else {
                Result.Error(readErrorMessage(response))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    private fun readErrorMessage(response: Response<*>): String {
        val fallback = "Lỗi máy chủ: ${response.code()}"
        val raw = response.errorBody()?.string() ?: return fallback
        return try {
            val type = object : TypeToken<ApiResponse<Any>>() {}.type
            val body: ApiResponse<Any> = gson.fromJson(raw, type)
            body.message ?: fallback
        } catch (e: Exception) {
            raw.ifBlank { fallback }
        }
    }
}
