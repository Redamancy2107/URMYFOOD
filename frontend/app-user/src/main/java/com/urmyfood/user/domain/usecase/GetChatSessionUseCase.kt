package com.urmyfood.user.domain.usecase

import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.repository.ChatRepository

class GetChatSessionUseCase(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(shopId: Long): Result<ChatSession> {
        val token = tokenManager.getAccessToken() ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.getOrCreateSession("Bearer $token", shopId)
    }
}
