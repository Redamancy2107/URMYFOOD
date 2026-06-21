package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Comment
import com.urmyfood.shop.domain.repository.PostRepository

class AddCommentUseCase(
    private val repository: PostRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(postId: String, content: String, parentId: String? = null): Result<Comment> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.addComment("Bearer $token", postId, content, parentId)
    }
}
