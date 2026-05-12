package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository

class GetPostsUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(): Result<List<FoodPost>> = repository.getPosts()
}
