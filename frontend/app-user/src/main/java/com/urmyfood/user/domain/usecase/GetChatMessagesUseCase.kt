package com.urmyfood.user.domain.usecase

import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.repository.ChatRepository

class GetChatMessagesUseCase(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(sessionId: Long): Result<List<ChatMessage>> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.getMessages("Bearer $token", sessionId)
    }
}
