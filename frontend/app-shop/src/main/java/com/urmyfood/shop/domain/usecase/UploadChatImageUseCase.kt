package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.repository.ChatRepository
import okhttp3.MultipartBody

class UploadChatImageUseCase(
    private val chatRepository: ChatRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(sessionId: Long, file: MultipartBody.Part): Result<String> {
        val token = tokenStore.getAccessToken() ?: return Result.Error("Không tìm thấy token đăng nhập")
        return chatRepository.uploadImage("Bearer $token", sessionId, file)
    }
}
