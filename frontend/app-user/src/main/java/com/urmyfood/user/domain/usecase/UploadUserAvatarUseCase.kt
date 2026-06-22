package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.UserProfile
import com.urmyfood.user.domain.repository.UserRepository
import okhttp3.MultipartBody

class UploadUserAvatarUseCase(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(file: MultipartBody.Part): Result<UserProfile> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return userRepository.uploadAvatar("Bearer $token", file)
    }
}
