package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.model.SavedPostResponse
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.SavedPostRepository

class GetSavedPostsUseCase(
    private val repository: SavedPostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<PageResult<FoodPost>> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return repository.getSavedPosts("Bearer $token", page, size)
    }
}

class GetSavedPostStateUseCase(
    private val repository: SavedPostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(postId: String): Result<SavedPostResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return repository.getSavedState("Bearer $token", postId)
    }
}

class SavePostUseCase(
    private val repository: SavedPostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(postId: String): Result<SavedPostResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return repository.savePost("Bearer $token", postId)
    }
}

class UnsavePostUseCase(
    private val repository: SavedPostRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(postId: String): Result<SavedPostResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return repository.unsavePost("Bearer $token", postId)
    }
}
