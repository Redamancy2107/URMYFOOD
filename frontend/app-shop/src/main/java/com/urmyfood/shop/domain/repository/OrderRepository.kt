package com.urmyfood.shop.domain.repository

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Order

interface OrderRepository {
    suspend fun getShopOrders(token: String): Result<List<Order>>
    suspend fun getShopOrderDetail(token: String, orderId: String): Result<Order>
    suspend fun updateOrderStatus(token: String, orderId: String, status: String, rejectReason: String?): Result<Order>
}
