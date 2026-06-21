package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.repository.PostRepository

class TogglePostStatusUseCase(
    private val repository: PostRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(postId: String, isActive: Boolean): Result<Post> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        val status = if (isActive) "ACTIVE" else "INACTIVE"
        return repository.updatePostStatus("Bearer $token", postId, status)
    }
}
