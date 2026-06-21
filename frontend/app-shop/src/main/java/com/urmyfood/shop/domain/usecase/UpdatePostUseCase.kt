package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.UpdatePostRequest
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.repository.PostRepository

class UpdatePostUseCase(
    private val repository: PostRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(postId: String, request: UpdatePostRequest): Result<Post> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.updatePost("Bearer $token", postId, request)
    }
}
