package com.urmyfood.shop.domain.repository

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.CreatePostRequest
import com.urmyfood.shop.data.model.UpdatePostRequest
import com.urmyfood.shop.domain.model.Comment
import com.urmyfood.shop.domain.model.Post
import okhttp3.MultipartBody

interface PostRepository {
    suspend fun getMyPosts(token: String, page: Int, size: Int): Result<List<Post>>
    suspend fun getPostById(token: String, postId: String): Result<Post>
    suspend fun createPost(token: String, request: CreatePostRequest): Result<Post>
    suspend fun updatePost(token: String, postId: String, request: UpdatePostRequest): Result<Post>
    suspend fun deletePost(token: String, postId: String): Result<Unit>
    suspend fun updatePostStatus(token: String, postId: String, status: String): Result<Post>
    suspend fun updateRemainingQuantity(token: String, postId: String, remainingQuantity: Int): Result<Post>
    suspend fun uploadPostImage(token: String, file: MultipartBody.Part): Result<String>
    suspend fun getComments(token: String, postId: String): Result<List<Comment>>
    suspend fun addComment(token: String, postId: String, content: String, parentId: String?): Result<Comment>
}
