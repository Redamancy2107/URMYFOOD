package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CommentResponse
import com.urmyfood.user.data.model.CreateCommentRequest
import com.urmyfood.user.data.model.LikeToggleResult
import com.urmyfood.user.data.model.PageResponse
import com.urmyfood.user.data.model.PostResponse
import com.urmyfood.user.data.model.SavedPostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApiService {

    @GET("api/v1/posts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String?
    ): Response<ApiResponse<PostResponse>>

    @GET("api/v1/posts")
    suspend fun getPosts(
        @Header("Authorization") token: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("anchor") anchor: String?
    ): Response<ApiResponse<PageResponse<PostResponse>>>

    @GET("api/v1/shops/{shopId}/posts")
    suspend fun getShopPosts(
        @Header("Authorization") token: String?,
        @Path("shopId") shopId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<PostResponse>>>

    @GET("api/v1/posts/search")
    suspend fun searchPosts(
        @Header("Authorization") token: String?,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("anchor") anchor: String?
    ): Response<ApiResponse<PageResponse<PostResponse>>>

    @GET("api/v1/posts/saved")
    suspend fun getSavedPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<PostResponse>>>

    @POST("api/v1/posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<LikeToggleResult>>

    @DELETE("api/v1/posts/{postId}/like")
    suspend fun unlikePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<LikeToggleResult>>

    @GET("api/v1/posts/{postId}/saved")
    suspend fun getSavedState(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<SavedPostResponse>>

    @POST("api/v1/posts/{postId}/saved")
    suspend fun savePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<SavedPostResponse>>

    @DELETE("api/v1/posts/{postId}/saved")
    suspend fun unsavePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<SavedPostResponse>>

    @GET("api/v1/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Header("Authorization") token: String,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int
    ): Response<ApiResponse<PageResponse<CommentResponse>>>

    @POST("api/v1/posts/{postId}/comments")
    suspend fun postComment(
        @Path("postId") postId: String,
        @Header("Authorization") token: String,
        @Body body: CreateCommentRequest
    ): Response<ApiResponse<CommentResponse>>
}
