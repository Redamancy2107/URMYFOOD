package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.CreatePostRequest
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.repository.PostRepository

class CreatePostUseCase(
    private val repository: PostRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(request: CreatePostRequest): Result<Post> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.createPost("Bearer $token", request)
    }
}
