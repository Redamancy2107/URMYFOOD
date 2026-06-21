package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.repository.ChatRepository

class SendChatMessageUseCase(private val repository: ChatRepository) {
    operator fun invoke(sessionId: Long, content: String) {
        repository.sendMessageViaWebSocket(sessionId, content)
    }
}
