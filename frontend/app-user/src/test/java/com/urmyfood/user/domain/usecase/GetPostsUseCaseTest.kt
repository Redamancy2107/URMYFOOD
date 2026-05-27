package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetPostsUseCaseTest {

    private val fakeToken = "fake-token"

    private val fakeTokenProvider = object : TokenProvider {
        override fun getAccessToken(): String = fakeToken
    }

    private fun fakeFoodPost(id: String = "1") = FoodPost(
        postId = id, dishName = "Phở bò", price = 60000.0, originalPrice = 70000.0,
        maxQuantity = 50, remainingQuantity = 30, endTime = null, isFlashSale = false,
        status = "ACTIVE", content = null, imageUrl = null, shopName = "Quán Phở", shopAvatarUrl = null
    )

    private fun makeRepository(result: Result<PageResult<FoodPost>>): PostRepository =
        object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?) = result
            override suspend fun searchPosts(token: String?, q: String, page: Int, size: Int, anchor: String?): Result<PageResult<FoodPost>> = Result.Success(PageResult(emptyList(), 0, false))
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int): Result<PageResult<Comment>> = Result.Success(PageResult(emptyList(), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String, parentId: String?): Result<Comment> = Result.Success(Comment("", "", null, "", ""))
        }

    @Test
    fun `invoke delegates to repository with bearer token`() = runTest {
        var receivedToken: String? = null
        val repo = object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?): Result<PageResult<FoodPost>> {
                receivedToken = token
                return Result.Success(PageResult(emptyList(), 0, false))
            }
            override suspend fun searchPosts(token: String?, q: String, page: Int, size: Int, anchor: String?) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int) = Result.Success(PageResult<Comment>(emptyList(), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String, parentId: String?) = Result.Success(Comment("", "", null, "", ""))
        }
        val useCase = GetPostsUseCase(repo, fakeTokenProvider)

        useCase(page = 0)

        assertEquals("Bearer $fakeToken", receivedToken)
    }

    @Test
    fun `invoke returns Success when repository returns Success`() = runTest {
        val posts = listOf(fakeFoodPost("10"), fakeFoodPost("11"))
        val pageResult = PageResult(posts, 0, false)
        val useCase = GetPostsUseCase(makeRepository(Result.Success(pageResult)), fakeTokenProvider)

        val result = useCase(page = 0)

        assertTrue(result is Result.Success)
        assertEquals(posts, (result as Result.Success).data.items)
    }

    @Test
    fun `invoke returns Error when repository returns Error`() = runTest {
        val errorMessage = "Lỗi server: 500"
        val useCase = GetPostsUseCase(makeRepository(Result.Error(errorMessage)), fakeTokenProvider)

        val result = useCase(page = 0)

        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `invoke passes null token when tokenProvider returns null`() = runTest {
        var receivedToken: String? = "sentinel"
        val nullTokenProvider = object : TokenProvider {
            override fun getAccessToken(): String? = null
        }
        val repo = object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?): Result<PageResult<FoodPost>> {
                receivedToken = token
                return Result.Success(PageResult(emptyList(), 0, false))
            }
            override suspend fun searchPosts(token: String?, q: String, page: Int, size: Int, anchor: String?) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int) = Result.Success(PageResult<Comment>(emptyList(), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String, parentId: String?) = Result.Success(Comment("", "", null, "", ""))
        }
        val useCase = GetPostsUseCase(repo, nullTokenProvider)

        useCase(page = 0)

        assertEquals(null, receivedToken)
    }

    @Test
    fun `invoke returns Success with empty list when repository returns empty page`() = runTest {
        val useCase = GetPostsUseCase(makeRepository(Result.Success(PageResult(emptyList(), 0, false))), fakeTokenProvider)

        val result = useCase(page = 0)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.items.isEmpty())
    }
}
