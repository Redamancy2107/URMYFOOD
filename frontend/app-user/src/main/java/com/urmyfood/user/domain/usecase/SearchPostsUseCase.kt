package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository

class SearchPostsUseCase(
    private val repository: PostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(query: String, page: Int, size: Int = PAGE_SIZE, anchor: String? = null): Result<PageResult<FoodPost>> {
        val token = tokenProvider.getAccessToken()?.let { "Bearer $it" }
        return repository.searchPosts(token, query, page, size, anchor)
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
