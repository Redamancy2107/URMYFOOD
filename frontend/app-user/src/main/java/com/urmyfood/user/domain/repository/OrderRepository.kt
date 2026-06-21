package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.OrderResponse
import com.urmyfood.user.domain.model.Result

interface OrderRepository {
    suspend fun checkout(
        token: String,
        paymentMethod: String,
        deliveryAddress: String,
        voucherId: Long?,
        note: String?,
        voucherCode: String? = null
    ): Result<OrderResponse>

    suspend fun getOrders(token: String): Result<List<OrderResponse>>
    suspend fun getOrderDetail(token: String, orderId: String): Result<OrderResponse>
    suspend fun cancelOrder(token: String, orderId: String, cancelReason: String): Result<OrderResponse>
    suspend fun createPayOsPayment(token: String, orderId: String): Result<com.urmyfood.user.data.model.PayOsPaymentResponse>
}
