package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.Result

interface PostRepository {
    suspend fun getPosts(token: String?): Result<List<FoodPost>>
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult>
    suspend fun getComments(postId: String): Result<List<Comment>>
    suspend fun postComment(postId: String, content: String, token: String): Result<Comment>
}
