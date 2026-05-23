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
class SearchPostsUseCaseTest {

    private fun fakePost(id: String) = FoodPost(id, "Post $id", 60000.0, 70000.0, 50, 30, null, false, "ACTIVE", null, null, "Shop", null)

    private val fakeToken = object : TokenProvider {
        override fun getAccessToken() = "tok"
    }

    private fun makeRepo(result: (String, Int, Int) -> Result<PageResult<FoodPost>>): PostRepository =
        object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun searchPosts(token: String?, query: String, page: Int, size: Int) = result(query, page, size)
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, page: Int, size: Int) = Result.Success(PageResult<Comment>(emptyList(), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String) = Result.Success(Comment("", "", null, "", ""))
        }

    @Test
    fun `invoke returns Success with results when repository returns posts`() = runTest {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val useCase = SearchPostsUseCase(makeRepo { _, _, _ -> Result.Success(PageResult(posts, 0, false)) }, fakeToken)

        val result = useCase("pho", page = 0)

        assertTrue(result is Result.Success)
        assertEquals(posts, (result as Result.Success).data.items)
    }

    @Test
    fun `invoke returns Error when repository returns Error`() = runTest {
        val useCase = SearchPostsUseCase(makeRepo { _, _, _ -> Result.Error("Search failed") }, fakeToken)

        val result = useCase("pho", page = 0)

        assertTrue(result is Result.Error)
        assertEquals("Search failed", (result as Result.Error).message)
    }

    @Test
    fun `invoke passes query and pagination params to repository`() = runTest {
        var receivedQuery = ""
        var receivedPage = -1
        var receivedSize = -1
        val useCase = SearchPostsUseCase(makeRepo { q, p, s ->
            receivedQuery = q
            receivedPage = p
            receivedSize = s
            Result.Success(PageResult(emptyList(), p, false))
        }, fakeToken)

        useCase("bun bo", page = 2, size = 10)

        assertEquals("bun bo", receivedQuery)
        assertEquals(2, receivedPage)
        assertEquals(10, receivedSize)
    }

    @Test
    fun `invoke returns empty page when no results match`() = runTest {
        val useCase = SearchPostsUseCase(makeRepo { _, _, _ -> Result.Success(PageResult(emptyList(), 0, false)) }, fakeToken)

        val result = useCase("xyz_no_match", page = 0)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.items.isEmpty())
    }
}
