package com.urmyfood.user.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.data.model.AddCartItemRequest
import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CartResponse
import com.urmyfood.user.data.model.UpdateCartItemRequest
import com.urmyfood.user.data.remote.CartApiService
import com.urmyfood.user.data.util.toUserMessage
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.CartRepository
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class CartRepositoryImpl(
    private val cartApiService: CartApiService
) : CartRepository {

    private val gson = Gson()

    override suspend fun getCart(token: String): Result<CartResponse> {
        return safeApiCall { cartApiService.getCart(token) }
    }

    override suspend fun addItem(token: String, postId: String, quantity: Int): Result<CartResponse> {
        return safeApiCall { cartApiService.addItem(token, AddCartItemRequest(postId, quantity)) }
    }

    override suspend fun updateItem(token: String, itemId: String, quantity: Int): Result<CartResponse> {
        return safeApiCall { cartApiService.updateItem(token, itemId, UpdateCartItemRequest(quantity)) }
    }

    override suspend fun deleteItem(token: String, itemId: String): Result<Unit> {
        return try {
            val response = cartApiService.deleteItem(token, itemId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(readErrorMessage(response))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<ApiResponse<T>>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data)
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
