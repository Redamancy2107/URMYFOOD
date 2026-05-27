package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository

class GetPostUseCase(
    private val repository: PostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(postId: String): Result<FoodPost> {
        val token = tokenProvider.getAccessToken()?.let { "Bearer $it" }
        return repository.getPost(postId, token)
    }
}
