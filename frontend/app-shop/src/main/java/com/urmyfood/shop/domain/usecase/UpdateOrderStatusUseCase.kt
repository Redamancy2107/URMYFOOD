package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.domain.repository.OrderRepository

class UpdateOrderStatusUseCase(
    private val repository: OrderRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(orderId: String, status: String, rejectReason: String? = null): Result<Order> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.updateOrderStatus("Bearer $token", orderId, status, rejectReason)
    }
}
