package com.urmyfood.user.presentation.main.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.GuestRepository
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.usecase.GetPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Fakes

    private val fakeTokenProvider = object : TokenProvider {
        override fun getAccessToken(): String? = "test_token"
    }

    private val fakeGuestRepository = object : GuestRepository {
        override fun isGuest(): Boolean = false
        override fun setGuest() {}
        override fun clearGuest() {}
    }

    private fun fakePosts() = listOf(
        FoodPost(
            postId = "1",
            dishName = "Bún bò Huế",
            price = 45000.0,
            originalPrice = 55000.0,
            maxQuantity = 100,
            remainingQuantity = 80,
            endTime = null,
            isFlashSale = false,
            status = "ACTIVE",
            content = "Ngon lắm",
            imageUrl = null,
            shopName = "Quán Ngon",
            shopAvatarUrl = null
        )
    )

    private fun fakePostRepository(
        postsResult: Result<List<FoodPost>> = Result.Success(emptyList()),
        likeResult: Result<LikeToggleResult> = Result.Error("not stubbed")
    ): PostRepository = object : PostRepository {
        override suspend fun getPosts(token: String?): Result<List<FoodPost>> = postsResult
        override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = likeResult
        override suspend fun getComments(postId: String, page: Int, size: Int): Result<PageResult<Comment>> =
            throw UnsupportedOperationException()
        override suspend fun postComment(postId: String, content: String, token: String): Result<Comment> =
            throw UnsupportedOperationException()
    }

    private fun makeViewModel(result: Result<List<FoodPost>>): HomeViewModel {
        val repo = fakePostRepository(postsResult = result)
        return HomeViewModel(
            GetPostsUseCase(repo, fakeTokenProvider),
            ToggleLikeUseCase(repo, fakeTokenProvider),
            fakeGuestRepository
        )
    }

    // Tests

    @Test
    fun `initial uiState is Loading before coroutine executes`() {
        val viewModel = makeViewModel(Result.Success(emptyList()))
        assertEquals(NewsfeedUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadPosts emits Success with posts when repository returns Success`() = runTest(testDispatcher) {
        val posts = fakePosts()
        val viewModel = makeViewModel(Result.Success(posts))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success state but got $state", state is NewsfeedUiState.Success)
        assertEquals(posts, (state as NewsfeedUiState.Success).posts)
    }

    @Test
    fun `loadPosts emits Error with message when repository returns Error`() = runTest(testDispatcher) {
        val errorMessage = "Không thể kết nối đến server"
        val viewModel = makeViewModel(Result.Error(errorMessage))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Error state but got $state", state is NewsfeedUiState.Error)
        assertEquals(errorMessage, (state as NewsfeedUiState.Error).message)
    }

    @Test
    fun `loadPosts resets to Loading before fetching then resolves to Success`() = runTest(testDispatcher) {
        val posts = fakePosts()
        val deferred = CompletableDeferred<Result<List<FoodPost>>>()
        val blockingRepo = object : PostRepository {
            override suspend fun getPosts(token: String?): Result<List<FoodPost>> = deferred.await()
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> =
                throw UnsupportedOperationException()
            override suspend fun getComments(postId: String, page: Int, size: Int): Result<PageResult<Comment>> =
                throw UnsupportedOperationException()
            override suspend fun postComment(postId: String, content: String, token: String): Result<Comment> =
                throw UnsupportedOperationException()
        }
        val viewModel = HomeViewModel(
            GetPostsUseCase(blockingRepo, fakeTokenProvider),
            ToggleLikeUseCase(blockingRepo, fakeTokenProvider),
            fakeGuestRepository
        )

        testDispatcher.scheduler.runCurrent()
        assertEquals(NewsfeedUiState.Loading, viewModel.uiState.value)

        deferred.complete(Result.Success(posts))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is NewsfeedUiState.Success)
    }

    @Test
    fun `loadPosts with empty list emits Success with empty list`() = runTest(testDispatcher) {
        val viewModel = makeViewModel(Result.Success(emptyList()))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is NewsfeedUiState.Success)
        assertTrue((state as NewsfeedUiState.Success).posts.isEmpty())
    }
}
