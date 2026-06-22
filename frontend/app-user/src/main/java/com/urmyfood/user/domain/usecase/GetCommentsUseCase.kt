package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository

class GetCommentsUseCase(
    private val repository: PostRepository,
    private val tokenProvider: TokenProvider
) {

    companion object {
        const val PAGE_SIZE = 15
    }

    suspend operator fun invoke(postId: String, cursor: String? = null, size: Int = PAGE_SIZE): Result<PageResult<Comment>> {
        val token = tokenProvider.getAccessToken()
            ?: return Result.Error("Vui lòng đăng nhập")
        return repository.getComments(postId, "Bearer $token", cursor, size)
    }
}
