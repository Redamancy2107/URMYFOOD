package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCommentsUseCaseTest {

    private fun makeRepo(): PostRepository =
        object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun searchPosts(token: String?, query: String, page: Int, size: Int, anchor: String?) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int) = Result.Success(PageResult(listOf(Comment("c1", "User", null, "Hi", "now")), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String, parentId: String?) = Result.Success(Comment("c1", "User", null, content, "now"))
            override suspend fun getPost(postId: String, token: String?) = Result.Error("Not implemented")
        }

    @Test
    fun `invoke returns Error when token is missing`() = runTest {
        val tokenProvider = object : TokenProvider {
            override fun getAccessToken(): String? = null
        }
        val useCase = GetCommentsUseCase(makeRepo(), tokenProvider)

        val result = useCase("p1", cursor = null)

        assertTrue(result is Result.Error)
        assertEquals("Vui lòng đăng nhập", (result as Result.Error).message)
    }

    @Test
    fun `invoke sends bearer token to repository`() = runTest {
        var receivedToken = ""
        val repo = object : PostRepository by makeRepo() {
            override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int): Result<PageResult<Comment>> {
                receivedToken = token
                return Result.Success(PageResult(emptyList(), 0, false))
            }
        }
        val tokenProvider = object : TokenProvider {
            override fun getAccessToken() = "tok"
        }
        val useCase = GetCommentsUseCase(repo, tokenProvider)

        useCase("p1", cursor = null)

        assertEquals("Bearer tok", receivedToken)
    }
}
