package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CommentResponse
import com.urmyfood.user.data.model.CreateCommentRequest
import com.urmyfood.user.data.model.LikeToggleResult
import com.urmyfood.user.data.model.PageResponse
import com.urmyfood.user.data.model.PostResponse
import com.urmyfood.user.data.remote.PostApiService
import com.urmyfood.user.domain.model.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryImplTest {

    // Helpers

    private fun fakePostResponse(id: String = "1") = PostResponse(
        postId = id,
        dishName = "Cơm tấm",
        price = 35000.0,
        originalPrice = 40000.0,
        maxQuantity = 200,
        remainingQuantity = 150,
        endTime = null,
        isFlashSale = false,
        status = "ACTIVE",
        content = "Cơm tấm sườn bì chả",
        imageUrl = "https://example.com/image.jpg",
        shopName = "Quán Cơm Ngon",
        shopAvatarUrl = null,
        createdAt = "2024-01-01T00:00:00Z"
    )

    private fun fakeApiService(
        getPostsBlock: suspend (String?) -> Response<ApiResponse<List<PostResponse>>> = {
            throw UnsupportedOperationException("not stubbed")
        }
    ): PostApiService = object : PostApiService {
        override suspend fun getPosts(token: String?): Response<ApiResponse<List<PostResponse>>> =
            getPostsBlock(token)

        override suspend fun likePost(postId: String, token: String): Response<ApiResponse<LikeToggleResult>> =
            throw UnsupportedOperationException("not stubbed")

        override suspend fun unlikePost(postId: String, token: String): Response<ApiResponse<LikeToggleResult>> =
            throw UnsupportedOperationException("not stubbed")

        override suspend fun getComments(
            postId: String, page: Int, size: Int
        ): Response<ApiResponse<PageResponse<CommentResponse>>> =
            throw UnsupportedOperationException("not stubbed")

        override suspend fun postComment(
            postId: String, token: String, body: CreateCommentRequest
        ): Response<ApiResponse<CommentResponse>> =
            throw UnsupportedOperationException("not stubbed")
    }

    private fun errorResponseBody() =
        "{}".toResponseBody("application/json".toMediaTypeOrNull())

    // Success path

    @Test
    fun `getPosts returns Success with mapped domain models when API returns successful response`() =
        runTest {
            val postResponses = listOf(fakePostResponse("1"), fakePostResponse("2"))
            val apiService = fakeApiService {
                Response.success(ApiResponse(success = true, message = "OK", data = postResponses))
            }
            val repository = PostRepositoryImpl(apiService)

            val result = repository.getPosts(null)

            assertTrue("Expected Result.Success but got $result", result is Result.Success)
            val posts = (result as Result.Success).data
            assertEquals(2, posts.size)
            assertEquals("1", posts[0].postId)
            assertEquals("Cơm tấm", posts[0].dishName)
            assertEquals(35000.0, posts[0].price, 0.0)
            assertEquals("2", posts[1].postId)
        }

    @Test
    fun `getPosts returns Success with empty list when API returns empty data array`() = runTest {
        val apiService = fakeApiService {
            Response.success(ApiResponse(success = true, message = "OK", data = emptyList()))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }

    // API-level error paths

    @Test
    fun `getPosts returns Error when API body reports success=false`() = runTest {
        val errorMsg = "Không có bài viết nào"
        val apiService = fakeApiService {
            Response.success(ApiResponse(success = false, message = errorMsg, data = null))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue("Expected Result.Error but got $result", result is Result.Error)
        assertEquals(errorMsg, (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns fallback Error message when API body has success=false and null message`() =
        runTest {
            val apiService = fakeApiService {
                Response.success(ApiResponse<List<PostResponse>>(success = false, message = null, data = null))
            }
            val repository = PostRepositoryImpl(apiService)

            val result = repository.getPosts(null)

            assertTrue(result is Result.Error)
            assertEquals("Không thể tải danh sách bài viết", (result as Result.Error).message)
        }

    @Test
    fun `getPosts returns Error when API body has success=true but null data`() = runTest {
        val apiService = fakeApiService {
            Response.success(ApiResponse<List<PostResponse>>(success = true, message = "No data", data = null))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue(result is Result.Error)
    }

    // HTTP error paths

    @Test
    fun `getPosts returns Error with server code when API returns HTTP 404`() = runTest {
        val apiService = fakeApiService { Response.error(404, errorResponseBody()) }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue("Expected Result.Error but got $result", result is Result.Error)
        assertEquals("Lỗi server: 404", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error with server code when API returns HTTP 500`() = runTest {
        val apiService = fakeApiService { Response.error(500, errorResponseBody()) }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue(result is Result.Error)
        assertEquals("Lỗi server: 500", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error with server code when API returns HTTP 401`() = runTest {
        val apiService = fakeApiService { Response.error(401, errorResponseBody()) }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue(result is Result.Error)
        assertEquals("Lỗi server: 401", (result as Result.Error).message)
    }

    // Network / exception paths

    @Test
    fun `getPosts returns Error when network throws IOException`() = runTest {
        val apiService = fakeApiService { throw IOException("Network unreachable") }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue("Expected Result.Error but got $result", result is Result.Error)
        assertEquals("Network unreachable", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns fallback Error message when IOException has null message`() = runTest {
        val apiService = fakeApiService { throw IOException() }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue(result is Result.Error)
        assertEquals("Không thể kết nối đến server", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error when unexpected RuntimeException is thrown`() = runTest {
        val apiService = fakeApiService { throw RuntimeException("Unexpected failure") }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts(null)

        assertTrue(result is Result.Error)
        assertEquals("Unexpected failure", (result as Result.Error).message)
    }

    // getComments — pagination

    private fun fakeCommentResponse(id: String = "c1") = CommentResponse(
        commentId = id,
        authorName = "Nguyễn A",
        authorAvatarUrl = null,
        content = "Ngon lắm",
        createdAt = "2024-01-01T00:00:00Z"
    )

    private fun fakePageResponse(
        content: List<CommentResponse>,
        page: Int = 0,
        size: Int = 20,
        totalElements: Long = content.size.toLong(),
        hasNext: Boolean = false
    ) = PageResponse(
        content = content,
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = 1,
        hasNext = hasNext
    )

    private fun fakeApiServiceWithComments(
        block: suspend (String, Int, Int) -> Response<ApiResponse<PageResponse<CommentResponse>>>
    ): PostApiService = object : PostApiService {
        override suspend fun getPosts(token: String?): Response<ApiResponse<List<PostResponse>>> =
            throw UnsupportedOperationException()
        override suspend fun likePost(postId: String, token: String): Response<ApiResponse<LikeToggleResult>> =
            throw UnsupportedOperationException()
        override suspend fun unlikePost(postId: String, token: String): Response<ApiResponse<LikeToggleResult>> =
            throw UnsupportedOperationException()
        override suspend fun getComments(postId: String, page: Int, size: Int) = block(postId, page, size)
        override suspend fun postComment(postId: String, token: String, body: CreateCommentRequest): Response<ApiResponse<CommentResponse>> =
            throw UnsupportedOperationException()
    }

    @Test
    fun `getComments returns PageResult with mapped comments on success`() = runTest {
        val comments = listOf(fakeCommentResponse("c1"), fakeCommentResponse("c2"))
        val apiService = fakeApiServiceWithComments { _, _, _ ->
            Response.success(ApiResponse(true, "OK", fakePageResponse(comments, page = 0, hasNext = true)))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getComments("post1", 0, 20)

        assertTrue("Expected Result.Success but got $result", result is Result.Success)
        val pageResult = (result as Result.Success).data
        assertEquals(2, pageResult.items.size)
        assertEquals("c1", pageResult.items[0].commentId)
        assertEquals("c2", pageResult.items[1].commentId)
        assertEquals(0, pageResult.page)
        assertTrue(pageResult.hasNext)
    }

    @Test
    fun `getComments returns hasNext=false on last page`() = runTest {
        val apiService = fakeApiServiceWithComments { _, _, _ ->
            Response.success(ApiResponse(true, "OK", fakePageResponse(listOf(fakeCommentResponse()), hasNext = false)))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getComments("post1", 1, 20)

        assertTrue(result is Result.Success)
        assertFalse((result as Result.Success).data.hasNext)
    }

    @Test
    fun `getComments returns Error when API reports success=false`() = runTest {
        val apiService = fakeApiServiceWithComments { _, _, _ ->
            Response.success(ApiResponse<PageResponse<CommentResponse>>(false, "Không tìm thấy bài viết", null))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getComments("post1", 0, 20)

        assertTrue(result is Result.Error)
        assertEquals("Không tìm thấy bài viết", (result as Result.Error).message)
    }

    @Test
    fun `getComments returns Error on HTTP 404`() = runTest {
        val apiService = fakeApiServiceWithComments { _, _, _ ->
            Response.error(404, errorResponseBody())
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getComments("post1", 0, 20)

        assertTrue(result is Result.Error)
        assertEquals("Lỗi server: 404", (result as Result.Error).message)
    }

    @Test
    fun `getComments returns Error on IOException`() = runTest {
        val apiService = fakeApiServiceWithComments { _, _, _ ->
            throw IOException("timeout")
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getComments("post1", 0, 20)

        assertTrue(result is Result.Error)
        assertEquals("timeout", (result as Result.Error).message)
    }
}
