package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.model.CartResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.CartRepository

class GetCartUseCase(
    private val cartRepository: CartRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(): Result<CartResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để xem giỏ hàng")
        return cartRepository.getCart("Bearer $token")
    }
}

class AddToCartUseCase(
    private val cartRepository: CartRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(postId: String, quantity: Int): Result<CartResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để thêm món")
        return cartRepository.addItem("Bearer $token", postId, quantity)
    }
}

class UpdateCartItemUseCase(
    private val cartRepository: CartRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(itemId: String, quantity: Int): Result<CartResponse> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để cập nhật giỏ hàng")
        return cartRepository.updateItem("Bearer $token", itemId, quantity)
    }
}

class DeleteCartItemUseCase(
    private val cartRepository: CartRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập để cập nhật giỏ hàng")
        return cartRepository.deleteItem("Bearer $token", itemId)
    }
}
