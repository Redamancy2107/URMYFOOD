package com.urmyfood.shop.domain.usecase

import com.urmyfood.shop.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    operator fun invoke(sessionId: Long, content: String) {
        repository.sendMessageViaWebSocket(sessionId, content)
    }
}
