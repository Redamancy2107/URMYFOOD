package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.repository.ChatRepository

class GetChatSessionUseCase(
    private val repository: ChatRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(shopId: Long): Result<ChatSession> {
        val token = tokenStore.getAccessToken() ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.getOrCreateSession("Bearer $token", shopId)
    }
}
