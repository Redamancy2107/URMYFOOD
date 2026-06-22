package com.urmyfood.shop.data.repository

import com.google.gson.Gson
import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.UpdateOrderStatusRequest
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.data.remote.OrderApiService
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.domain.repository.OrderRepository

class OrderRepositoryImpl(
    private val apiService: OrderApiService
) : OrderRepository {

    private val gson = Gson()

    override suspend fun getShopOrders(token: String): Result<List<Order>> {
        return try {
            val response = apiService.getShopOrders(token)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.map { it.toDomain() })
                } else {
                    Result.Error(body?.message ?: "Không thể lấy danh sách đơn hàng")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun getShopOrderDetail(token: String, orderId: String): Result<Order> {
        return try {
            val response = apiService.getShopOrderDetail(token, orderId)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy chi tiết đơn hàng")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: String, rejectReason: String?): Result<Order> {
        return try {
            val request = UpdateOrderStatusRequest(status = status, rejectReason = rejectReason)
            val response = apiService.updateOrderStatus(token, orderId, request)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật trạng thái đơn hàng")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
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
