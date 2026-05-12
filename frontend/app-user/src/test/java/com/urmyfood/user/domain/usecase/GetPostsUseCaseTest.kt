package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetPostsUseCaseTest {

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun fakeFoodPost(id: String = "1") = FoodPost(
        postId = id,
        dishName = "Phở bò",
        price = 60000.0,
        originalPrice = 70000.0,
        maxQuantity = 50,
        remainingQuantity = 30,
        endTime = null,
        isFlashSale = false,
        status = "ACTIVE",
        content = null,
        imageUrl = null,
        shopName = "Quán Phở",
        shopAvatarUrl = null
    )

    private fun makeFakeRepository(result: Result<List<FoodPost>>): PostRepository =
        object : PostRepository {
            var callCount = 0

            override suspend fun getPosts(): Result<List<FoodPost>> {
                callCount++
                return result
            }
        }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `invoke delegates to repository exactly once`() = runTest {
        var callCount = 0
        val fakeRepository = object : PostRepository {
            override suspend fun getPosts(): Result<List<FoodPost>> {
                callCount++
                return Result.Success(emptyList())
            }
        }
        val useCase = GetPostsUseCase(fakeRepository)

        useCase()

        assertEquals("repository.getPosts() should be called exactly once", 1, callCount)
    }

    @Test
    fun `invoke returns Success when repository returns Success`() = runTest {
        val posts = listOf(fakeFoodPost("10"), fakeFoodPost("11"))
        val useCase = GetPostsUseCase(makeFakeRepository(Result.Success(posts)))

        val result = useCase()

        assertTrue("Expected Result.Success but got $result", result is Result.Success)
        assertEquals(posts, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns Error when repository returns Error`() = runTest {
        val errorMessage = "Lỗi server: 500"
        val useCase = GetPostsUseCase(makeFakeRepository(Result.Error(errorMessage)))

        val result = useCase()

        assertTrue("Expected Result.Error but got $result", result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `invoke propagates Error code from repository`() = runTest {
        val errorMessage = "Unauthorized"
        val errorCode = 401
        val useCase = GetPostsUseCase(
            object : PostRepository {
                override suspend fun getPosts() = Result.Error(errorMessage, errorCode)
            }
        )

        val result = useCase()

        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals(errorMessage, error.message)
        assertEquals(errorCode, error.code)
    }

    @Test
    fun `invoke returns Success with empty list when repository returns empty list`() = runTest {
        val useCase = GetPostsUseCase(makeFakeRepository(Result.Success(emptyList())))

        val result = useCase()

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }
}
