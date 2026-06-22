package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.OrderResponse
import com.urmyfood.shop.data.model.UpdateOrderStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderApiService {
    @GET("api/v1/orders/shop/me")
    suspend fun getShopOrders(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<OrderResponse>>>

    @GET("api/v1/orders/shop/me/{orderId}")
    suspend fun getShopOrderDetail(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<ApiResponse<OrderResponse>>

    @PUT("api/v1/orders/shop/me/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<ApiResponse<OrderResponse>>
}
