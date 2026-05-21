package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository

class GetPostsUseCase(
    private val repository: PostRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<List<FoodPost>> {
        val token = tokenManager.getAccessToken()?.let { "Bearer $it" }
        return repository.getPosts(token)
    }
}
