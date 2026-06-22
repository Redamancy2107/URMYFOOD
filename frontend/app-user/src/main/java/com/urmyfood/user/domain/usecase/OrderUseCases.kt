package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.model.OrderResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.OrderRepository

class CheckoutUseCase(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(
        paymentMethod: String,
        deliveryAddress: String,
        note: String? = null,
        voucherCode: String? = null
    ): Result<OrderResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để đặt hàng")
        return orderRepository.checkout("Bearer $token", paymentMethod, deliveryAddress, null, note, voucherCode)
    }
}

class DirectCheckoutUseCase(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(
        postId: String,
        quantity: Int,
        paymentMethod: String,
        deliveryAddress: String,
        note: String? = null,
        voucherCode: String? = null
    ): Result<OrderResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để đặt hàng")
        return orderRepository.directCheckout(
            "Bearer $token",
            postId,
            quantity,
            paymentMethod,
            deliveryAddress,
            null,
            note,
            voucherCode
        )
    }
}

class GetOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(): Result<List<OrderResponse>> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để xem đơn hàng")
        return orderRepository.getOrders("Bearer $token")
    }
}

class CancelOrderUseCase(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(orderId: String, cancelReason: String): Result<OrderResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để hủy đơn hàng")
        return orderRepository.cancelOrder("Bearer $token", orderId, cancelReason)
    }
}

class CreatePayOsPaymentUseCase(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(orderId: String): Result<com.urmyfood.user.data.model.PayOsPaymentResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập")
        return orderRepository.createPayOsPayment("Bearer $token", orderId)
    }
}

class CheckPayOsStatusUseCase(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(orderId: String): Result<OrderResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập")
        return orderRepository.checkPayOsStatus("Bearer $token", orderId)
    }
}
