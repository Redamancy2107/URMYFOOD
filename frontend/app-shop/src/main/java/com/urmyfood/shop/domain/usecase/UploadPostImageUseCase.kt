package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.repository.PostRepository
import okhttp3.MultipartBody

class UploadPostImageUseCase(
    private val repository: PostRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(file: MultipartBody.Part): Result<String> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.uploadPostImage("Bearer $token", file)
    }
}
