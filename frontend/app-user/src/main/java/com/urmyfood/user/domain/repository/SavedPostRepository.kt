package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.SavedPostResponse
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result

interface SavedPostRepository {
    suspend fun getSavedPosts(token: String, page: Int, size: Int): Result<PageResult<FoodPost>>
    suspend fun getSavedState(token: String, postId: String): Result<SavedPostResponse>
    suspend fun savePost(token: String, postId: String): Result<SavedPostResponse>
    suspend fun unsavePost(token: String, postId: String): Result<SavedPostResponse>
}
