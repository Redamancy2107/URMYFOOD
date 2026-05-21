package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository

class GetCommentsUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(postId: String): Result<List<Comment>> =
        repository.getComments(postId)
}
