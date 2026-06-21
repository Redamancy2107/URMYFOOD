package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.repository.ChatRepository

class GetMessagesUseCase(
    private val repository: ChatRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(sessionId: Long): Result<List<ChatMessage>> {
        val token = tokenStore.getAccessToken() ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.getMessages("Bearer $token", sessionId)
    }
}
