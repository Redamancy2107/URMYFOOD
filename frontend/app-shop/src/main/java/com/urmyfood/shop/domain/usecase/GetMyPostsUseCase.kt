package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.repository.PostRepository

class GetMyPostsUseCase(
    private val repository: PostRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<List<Post>> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.getMyPosts("Bearer $token", page, size)
    }
}
