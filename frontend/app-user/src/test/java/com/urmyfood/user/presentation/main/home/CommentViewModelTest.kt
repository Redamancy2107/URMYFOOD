package com.urmyfood.user.presentation.main.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.usecase.GetCommentsUseCase
import com.urmyfood.user.domain.usecase.PostCommentUseCase
import com.urmyfood.user.domain.model.FoodPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // Helpers

    private fun fakeComment(id: String = "c1") = Comment(
        commentId = id,
        authorName = "User",
        authorAvatarUrl = null,
        content = "Ngon",
        createdAt = "2024-01-01T00:00:00Z"
    )

    private val fakeTokenProvider = object : TokenProvider {
        override fun getAccessToken(): String? = "token"
    }

    private fun fakeRepository(
        commentsResults: List<Result<PageResult<Comment>>> = emptyList(),
        postCommentResult: Result<Comment> = Result.Error("not stubbed")
    ): PostRepository {
        val resultQueue = ArrayDeque(commentsResults)
        return object : PostRepository {
            override suspend fun getPosts(token: String?): Result<List<FoodPost>> =
                throw UnsupportedOperationException()
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> =
                throw UnsupportedOperationException()
            override suspend fun getComments(postId: String, page: Int, size: Int): Result<PageResult<Comment>> =
                if (resultQueue.isNotEmpty()) resultQueue.removeFirst()
                else Result.Error("queue empty")
            override suspend fun postComment(postId: String, content: String, token: String): Result<Comment> =
                postCommentResult
        }
    }

    private fun makeViewModel(
        commentsResults: List<Result<PageResult<Comment>>>,
        postCommentResult: Result<Comment> = Result.Error("not stubbed")
    ): CommentViewModel {
        val repo = fakeRepository(commentsResults, postCommentResult)
        return CommentViewModel(GetCommentsUseCase(repo), PostCommentUseCase(repo, fakeTokenProvider))
    }

    // loadComments

    @Test
    fun `loadComments emits Loading then Success`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1"), fakeComment("c2")), page = 0, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0)))

        viewModel.loadComments("post1")
        assertEquals(CommentViewModel.UiState.Loading, viewModel.uiState.value)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CommentViewModel.UiState.Success)
        val comments = (state as CommentViewModel.UiState.Success).comments
        assertEquals(2, comments.size)
        assertEquals("c1", comments[0].commentId)
        assertEquals("c2", comments[1].commentId)
    }

    @Test
    fun `loadComments emits Error when repository returns Error`() = runTest(testDispatcher) {
        val viewModel = makeViewModel(listOf(Result.Error("Lỗi kết nối")))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CommentViewModel.UiState.Error)
        assertEquals("Lỗi kết nối", (state as CommentViewModel.UiState.Error).message)
    }

    @Test
    fun `loadComments replaces existing list on reload`() = runTest(testDispatcher) {
        val page0First = PageResult(listOf(fakeComment("old")), page = 0, hasNext = false)
        val page0Second = PageResult(listOf(fakeComment("new1"), fakeComment("new2")), page = 0, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0First), Result.Success(page0Second)))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(2, state.comments.size)
        assertEquals("new1", state.comments[0].commentId)
    }

    // loadMore

    @Test
    fun `loadMore appends older comments to accumulated list`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1"), fakeComment("c2")), page = 0, hasNext = true)
        val page1 = PageResult(listOf(fakeComment("c3"), fakeComment("c4")), page = 1, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0), Result.Success(page1)))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(4, state.comments.size)
        assertEquals("c1", state.comments[0].commentId)
        assertEquals("c3", state.comments[2].commentId)
    }

    @Test
    fun `loadMore does nothing when hasNext is false`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1")), page = 0, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0)))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(1, state.comments.size)
    }

    @Test
    fun `loadMore deduplicates items that overlap due to offset drift`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1"), fakeComment("c2")), page = 0, hasNext = true)
        // page1 overlaps c2 (offset drift after new insert)
        val page1 = PageResult(listOf(fakeComment("c2"), fakeComment("c3")), page = 1, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0), Result.Success(page1)))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(3, state.comments.size)
        assertEquals(listOf("c1", "c2", "c3"), state.comments.map { it.commentId })
    }

    // sendComment

    @Test
    fun `sendComment optimistically prepends comment then replaces with real data`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1")), page = 0, hasNext = false)
        val realComment = fakeComment("real_id")
        val viewModel = makeViewModel(listOf(Result.Success(page0)), postCommentResult = Result.Success(realComment))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendComment("post1", "Hello", "Tôi")

        // Optimistic: prepended immediately before coroutine completes
        val optimisticState = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(2, optimisticState.comments.size)
        assertEquals("Hello", optimisticState.comments[0].content)
        assertEquals("Tôi", optimisticState.comments[0].authorName)

        testDispatcher.scheduler.advanceUntilIdle()

        // After server response: optimistic replaced by real comment
        val finalState = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(2, finalState.comments.size)
        assertEquals("real_id", finalState.comments[0].commentId)
    }

    @Test
    fun `sendComment rolls back optimistic comment on error`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1")), page = 0, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0)), postCommentResult = Result.Error("Lỗi"))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendComment("post1", "Test", "Tôi")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(1, state.comments.size)
        assertEquals("c1", state.comments[0].commentId)
    }

    @Test
    fun `sendComment sets sendError on failure`() = runTest(testDispatcher) {
        val page0 = PageResult(emptyList<Comment>(), page = 0, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0)), postCommentResult = Result.Error("Vui lòng đăng nhập"))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendComment("post1", "Hi", "Tôi")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Vui lòng đăng nhập", viewModel.sendError.value)
    }

    @Test
    fun `sendComment does nothing when content is blank`() = runTest(testDispatcher) {
        val page0 = PageResult(listOf(fakeComment("c1")), page = 0, hasNext = false)
        val viewModel = makeViewModel(listOf(Result.Success(page0)))

        viewModel.loadComments("post1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendComment("post1", "   ", "Tôi")

        val state = viewModel.uiState.value as CommentViewModel.UiState.Success
        assertEquals(1, state.comments.size)
    }
}
