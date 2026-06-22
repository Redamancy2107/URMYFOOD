package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CommentResponse
import com.urmyfood.user.data.model.CreateCommentRequest
import com.urmyfood.user.data.model.LikeToggleResult
import com.urmyfood.user.data.model.PageResponse
import com.urmyfood.user.data.model.PostResponse
import com.urmyfood.user.data.remote.PostApiService
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryImplTest {

    private fun fakePostResponse(id: String = "1") = PostResponse(
        postId = id, dishName = "Cơm tấm", price = 35000.0, originalPrice = 40000.0,
        maxQuantity = 200, remainingQuantity = 150, endTime = null, isFlashSale = false,
        status = "ACTIVE", content = "Cơm tấm sườn bì chả",
        imageUrl = "https://example.com/image.jpg", shopName = "Quán Cơm Ngon",
        shopAvatarUrl = null, shopAddress = null, createdAt = "2024-01-01T00:00:00Z"
    )

    private fun fakePageResponse(vararg posts: PostResponse, hasNext: Boolean = false) = PageResponse(
        content = posts.toList(), page = 0, size = 20,
        totalElements = posts.size.toLong(), totalPages = 1, hasNext = hasNext
    )

    private fun errorBody() = "{}".toResponseBody("application/json".toMediaTypeOrNull())

    private fun makeApiService(
        getPostsBlock: suspend (String?, Int, Int, String?) -> Response<ApiResponse<PageResponse<PostResponse>>>
    ): PostApiService = object : PostApiService {
        override suspend fun getPost(postId: String, token: String?): Response<ApiResponse<PostResponse>> =
            Response.success(ApiResponse(true, "OK", fakePostResponse(postId)))
        override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?, category: String?) = getPostsBlock(token, page, size, anchor)
        override suspend fun searchPosts(token: String?, query: String, page: Int, size: Int, anchor: String?): Response<ApiResponse<PageResponse<PostResponse>>> =
            Response.success(ApiResponse(true, "OK", fakePageResponse()))
        override suspend fun likePost(postId: String, token: String): Response<ApiResponse<LikeToggleResult>> =
            Response.success(ApiResponse(true, "OK", LikeToggleResult(1, true)))
        override suspend fun unlikePost(postId: String, token: String): Response<ApiResponse<LikeToggleResult>> =
            Response.success(ApiResponse(true, "OK", LikeToggleResult(0, false)))
        override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int): Response<ApiResponse<PageResponse<CommentResponse>>> =
            Response.success(ApiResponse(true, "OK", PageResponse(emptyList(), 0, 20, 0L, 0, false, null, null)))
        override suspend fun postComment(postId: String, token: String, body: CreateCommentRequest): Response<ApiResponse<CommentResponse>> =
            Response.success(ApiResponse(true, "OK", CommentResponse("c1", "User", null, "test", "2024-01-01")))
    }

    @Test
    fun `getPosts returns Success with mapped domain models when API returns successful response`() = runTest {
        val postResponses = listOf(fakePostResponse("1"), fakePostResponse("2"))
        val apiService = makeApiService { _, _, _, _ ->
            Response.success(ApiResponse(true, "OK", fakePageResponse(*postResponses.toTypedArray())))
        }

        val result = PostRepositoryImpl(apiService).getPosts(token = null, page = 0, size = 20, anchor = null, category = null)

        assertTrue(result is Result.Success)
        val pageResult = (result as Result.Success<PageResult<*>>).data
        assertEquals(2, pageResult.items.size)
    }

    @Test
    fun `getPosts returns Success with empty list when API returns empty data`() = runTest {
        val apiService = makeApiService { _, _, _, _ ->
            Response.success(ApiResponse(true, "OK", fakePageResponse()))
        }

        val result = PostRepositoryImpl(apiService).getPosts(null, 0, 20, null, null)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.items.isEmpty())
    }

    @Test
    fun `getPosts returns Error when API body reports success=false`() = runTest {
        val errorMsg = "Không có bài viết nào"
        val apiService = makeApiService { _, _, _, _ ->
            Response.success(ApiResponse<PageResponse<PostResponse>>(false, errorMsg, null))
        }

        val result = PostRepositoryImpl(apiService).getPosts(null, 0, 20, null, null)

        assertTrue(result is Result.Error)
        assertEquals(errorMsg, (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error when API returns HTTP 404`() = runTest {
        val apiService = makeApiService { _, _, _, _ -> Response.error(404, errorBody()) }

        val result = PostRepositoryImpl(apiService).getPosts(null, 0, 20, null, null)

        assertTrue(result is Result.Error)
        assertEquals("Lỗi server: 404", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error when network throws IOException`() = runTest {
        val apiService = makeApiService { _, _, _, _ -> throw IOException("Network unreachable") }

        val result = PostRepositoryImpl(apiService).getPosts(null, 0, 20, null, null)

        assertTrue(result is Result.Error)
        assertEquals("Network unreachable", (result as Result.Error).message)
    }

    @Test
    fun `toggleLike returns mapped LikeToggleResult`() = runTest {
        val apiService = makeApiService { _, _, _, _ ->
            Response.success(ApiResponse(true, "OK", fakePageResponse()))
        }

        val result = PostRepositoryImpl(apiService).toggleLike("p1", isCurrentlyLiked = false, token = "Bearer tok")

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.likeCount)
        assertTrue(result.data.isLiked)
    }

    @Test
    fun `getComments returns mapped comments`() = runTest {
        val apiService = makeApiService { _, _, _, _ ->
            Response.success(ApiResponse(true, "OK", fakePageResponse()))
        }

        val result = PostRepositoryImpl(apiService).getComments("p1", "Bearer tok", cursor = null, size = 20)

        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.items.size)
    }
}
