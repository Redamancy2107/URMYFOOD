package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.AddCommentRequest
import com.urmyfood.shop.data.model.CommentDto
import com.urmyfood.shop.data.model.CommentPageDto
import com.urmyfood.shop.data.model.CreatePostRequest
import com.urmyfood.shop.data.model.PostDto
import com.urmyfood.shop.data.model.PostImageUploadResponse
import com.urmyfood.shop.data.model.PostPageDto
import com.urmyfood.shop.data.model.UpdatePostRequest
import com.urmyfood.shop.data.model.UpdatePostStatusRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApiService {

    @GET("api/v1/posts/mine")
    suspend fun getMyPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PostPageDto>>

    @GET("api/v1/posts/{postId}")
    suspend fun getPostById(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Response<ApiResponse<PostDto>>

    @POST("api/v1/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: CreatePostRequest
    ): Response<ApiResponse<PostDto>>

    @PUT("api/v1/posts/{postId}")
    suspend fun updatePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Body request: UpdatePostRequest
    ): Response<ApiResponse<PostDto>>

    @DELETE("api/v1/posts/{postId}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Response<ApiResponse<Void>>

    @PATCH("api/v1/posts/{postId}/status")
    suspend fun updatePostStatus(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Body request: UpdatePostStatusRequest
    ): Response<ApiResponse<PostDto>>

    @Multipart
    @POST("api/v1/posts/images")
    suspend fun uploadPostImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<PostImageUploadResponse>>

    @GET("api/v1/posts/{postId}/comments")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<CommentPageDto>>

    @POST("api/v1/posts/{postId}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Body request: AddCommentRequest
    ): Response<ApiResponse<CommentDto>>
}
