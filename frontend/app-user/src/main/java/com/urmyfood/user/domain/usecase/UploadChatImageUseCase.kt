package com.urmyfood.user.domain.usecase

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.repository.ChatRepository
import okhttp3.MultipartBody

class UploadChatImageUseCase(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(sessionId: Long, file: MultipartBody.Part): Result<String> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        return chatRepository.uploadImage(token, sessionId, file)
    }
}
