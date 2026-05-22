package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository

class ToggleLikeUseCase(
    private val repository: PostRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(postId: String, isCurrentlyLiked: Boolean): Result<LikeToggleResult> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Vui lòng đăng nhập")
        return repository.toggleLike(postId, isCurrentlyLiked, "Bearer $token")
    }
}
