package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository

class PostCommentUseCase(
    private val repository: PostRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(postId: String, content: String): Result<Comment> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Vui lòng đăng nhập")
        return repository.postComment(postId, content, "Bearer $token")
    }
}
