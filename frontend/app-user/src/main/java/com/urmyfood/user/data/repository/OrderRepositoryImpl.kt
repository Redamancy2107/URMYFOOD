package com.urmyfood.user.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CancelOrderRequest
import com.urmyfood.user.data.model.CheckoutRequest
import com.urmyfood.user.data.model.OrderResponse
import com.urmyfood.user.data.remote.OrderApiService
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class OrderRepositoryImpl(
    private val orderApiService: OrderApiService
) : OrderRepository {

    private val gson = Gson()

    override suspend fun checkout(
        token: String,
        paymentMethod: String,
        deliveryAddress: String,
        voucherId: Long?,
        note: String?,
        voucherCode: String?
    ): Result<OrderResponse> {
        return safeApiCall {
            orderApiService.checkout(token, CheckoutRequest(paymentMethod, deliveryAddress, voucherId, note, voucherCode))
        }
    }

    override suspend fun getOrders(token: String): Result<List<OrderResponse>> {
        return safeApiCall { orderApiService.getOrders(token) }
    }

    override suspend fun getOrderDetail(token: String, orderId: String): Result<OrderResponse> {
        return safeApiCall { orderApiService.getOrderDetail(token, orderId) }
    }

    override suspend fun cancelOrder(token: String, orderId: String, cancelReason: String): Result<OrderResponse> {
        return safeApiCall { orderApiService.cancelOrder(token, orderId, CancelOrderRequest(cancelReason)) }
    }

    override suspend fun createPayOsPayment(token: String, orderId: String): Result<com.urmyfood.user.data.model.PayOsPaymentResponse> {
        return safeApiCall { orderApiService.createPayOsPayment(token, orderId) }
    }

    override suspend fun checkPayOsStatus(token: String, orderId: String): Result<OrderResponse> {
        return safeApiCall { orderApiService.checkPayOsStatus(token, orderId) }
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
            Result.Error(e.message ?: "Không thể kết nối đến server")
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
