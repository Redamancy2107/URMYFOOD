package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CancelOrderRequest
import com.urmyfood.user.data.model.CheckoutRequest
import com.urmyfood.user.data.model.OrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderApiService {
    @POST("api/v1/orders/checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequest
    ): Response<ApiResponse<OrderResponse>>

    @GET("api/v1/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<OrderResponse>>>

    @GET("api/v1/orders/{orderId}")
    suspend fun getOrderDetail(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<ApiResponse<OrderResponse>>

    @PUT("api/v1/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<ApiResponse<OrderResponse>>

    @POST("api/v1/payments/payos/create/{orderId}")
    suspend fun createPayOsPayment(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<ApiResponse<com.urmyfood.user.data.model.PayOsPaymentResponse>>
}
