package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result

interface PostRepository {
    suspend fun getPosts(token: String?, page: Int, size: Int): Result<PageResult<FoodPost>>
    suspend fun searchPosts(token: String?, query: String, page: Int, size: Int): Result<PageResult<FoodPost>>
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult>
    suspend fun getComments(postId: String, token: String, page: Int, size: Int): Result<PageResult<Comment>>
    suspend fun postComment(postId: String, content: String, token: String): Result<Comment>
}
