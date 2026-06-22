package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository

class PostCommentUseCase(
    private val repository: PostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(postId: String, content: String, parentId: String? = null): Result<Comment> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập")
        return repository.postComment(postId, content, "Bearer $token", parentId)
    }
}
