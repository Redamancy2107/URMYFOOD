package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.repository.ChatRepository

class MarkAsReadUseCase(
    private val repository: ChatRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(sessionId: Long): Result<Unit> {
        val token = tokenStore.getAccessToken() ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.markAsRead("Bearer $token", sessionId)
    }
}
