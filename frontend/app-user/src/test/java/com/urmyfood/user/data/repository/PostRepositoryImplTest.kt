package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.PostResponse
import com.urmyfood.user.data.remote.PostApiService
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

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

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
        block: suspend () -> Response<ApiResponse<List<PostResponse>>>
    ): PostApiService = object : PostApiService {
        override suspend fun getPosts(): Response<ApiResponse<List<PostResponse>>> = block()
    }

    private fun errorResponseBody() =
        "{}".toResponseBody("application/json".toMediaTypeOrNull())

    // ---------------------------------------------------------------------------
    // Success path
    // ---------------------------------------------------------------------------

    @Test
    fun `getPosts returns Success with mapped domain models when API returns successful response`() =
        runTest {
            val postResponses = listOf(fakePostResponse("1"), fakePostResponse("2"))
            val apiService = fakeApiService {
                Response.success(ApiResponse(success = true, message = "OK", data = postResponses))
            }
            val repository = PostRepositoryImpl(apiService)

            val result = repository.getPosts()

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

        val result = repository.getPosts()

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }

    // ---------------------------------------------------------------------------
    // API-level error paths (HTTP 2xx but business logic failure)
    // ---------------------------------------------------------------------------

    @Test
    fun `getPosts returns Error when API body reports success=false`() = runTest {
        val errorMsg = "Không có bài viết nào"
        val apiService = fakeApiService {
            Response.success(ApiResponse(success = false, message = errorMsg, data = null))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

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

            val result = repository.getPosts()

            assertTrue(result is Result.Error)
            assertEquals("Không thể tải danh sách bài viết", (result as Result.Error).message)
        }

    @Test
    fun `getPosts returns Error when API body has success=true but null data`() = runTest {
        val apiService = fakeApiService {
            Response.success(ApiResponse<List<PostResponse>>(success = true, message = "No data", data = null))
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue(result is Result.Error)
    }

    // ---------------------------------------------------------------------------
    // HTTP error paths
    // ---------------------------------------------------------------------------

    @Test
    fun `getPosts returns Error with server code when API returns HTTP 404`() = runTest {
        val apiService = fakeApiService {
            Response.error(404, errorResponseBody())
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue("Expected Result.Error but got $result", result is Result.Error)
        assertEquals("Lỗi server: 404", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error with server code when API returns HTTP 500`() = runTest {
        val apiService = fakeApiService {
            Response.error(500, errorResponseBody())
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue(result is Result.Error)
        assertEquals("Lỗi server: 500", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error with server code when API returns HTTP 401`() = runTest {
        val apiService = fakeApiService {
            Response.error(401, errorResponseBody())
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue(result is Result.Error)
        assertEquals("Lỗi server: 401", (result as Result.Error).message)
    }

    // ---------------------------------------------------------------------------
    // Network / exception paths
    // ---------------------------------------------------------------------------

    @Test
    fun `getPosts returns Error when network throws IOException`() = runTest {
        val apiService = fakeApiService {
            throw IOException("Network unreachable")
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue("Expected Result.Error but got $result", result is Result.Error)
        assertEquals("Network unreachable", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns fallback Error message when IOException has null message`() = runTest {
        val apiService = fakeApiService {
            throw IOException()   // message == null
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue(result is Result.Error)
        assertEquals("Không thể kết nối đến server", (result as Result.Error).message)
    }

    @Test
    fun `getPosts returns Error when unexpected RuntimeException is thrown`() = runTest {
        val apiService = fakeApiService {
            throw RuntimeException("Unexpected failure")
        }
        val repository = PostRepositoryImpl(apiService)

        val result = repository.getPosts()

        assertTrue(result is Result.Error)
        assertEquals("Unexpected failure", (result as Result.Error).message)
    }
}
