package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.PostResponse
import retrofit2.Response
import retrofit2.http.GET

interface PostApiService {
    @GET("api/v1/posts")
    suspend fun getPosts(): Response<ApiResponse<List<PostResponse>>>
}
