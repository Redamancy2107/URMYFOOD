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
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun fakePosts(count: Int = 1) = (1..count).map {
        FoodPost("$it", "Post $it", 50000.0, 60000.0, 50, 40, null, false, "ACTIVE", null, null, "Shop", null)
    }

    private val fakeGuestRepo = object : GuestRepository {
        override fun isGuest(): Boolean = false
        override fun setGuest() = Unit
        override fun clearGuest() = Unit
    }

    private val fakeToken = object : TokenProvider { override fun getAccessToken() = "tok" }

    private fun makeFullFakeRepo(
        getPostsResult: (Int) -> Result<PageResult<FoodPost>> = { Result.Success(PageResult(emptyList(), it, false)) }
    ): PostRepository = object : PostRepository {
        override suspend fun getPosts(token: String?, page: Int, size: Int) = getPostsResult(page)
        override suspend fun searchPosts(token: String?, q: String, page: Int, size: Int) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
        override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
        override suspend fun getComments(postId: String, token: String, page: Int, size: Int) = Result.Success(PageResult<Comment>(emptyList(), 0, false))
        override suspend fun postComment(postId: String, content: String, token: String) = Result.Success(Comment("", "", null, "", ""))
    }

    private fun makeViewModel(
        getPostsResult: Result<PageResult<FoodPost>> = Result.Success(PageResult(emptyList(), 0, false))
    ): HomeViewModel {
        val repo = makeFullFakeRepo { getPostsResult }
        return HomeViewModel(
            GetPostsUseCase(repo, fakeToken),
            ToggleLikeUseCase(repo, fakeToken),
            fakeGuestRepo
        )
    }

    @Test
    fun `initial uiState is Loading before coroutine executes`() {
        val vm = makeViewModel()
        assertEquals(NewsfeedUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `loadPosts emits Success when repository returns Success`() = runTest(testDispatcher) {
        val posts = fakePosts(2)
        val vm = makeViewModel(Result.Success(PageResult(posts, 0, false)))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is NewsfeedUiState.Success)
        assertEquals(posts, (state as NewsfeedUiState.Success).posts)
    }

    @Test
    fun `loadPosts emits Error when repository returns Error`() = runTest(testDispatcher) {
        val vm = makeViewModel(Result.Error("Không thể kết nối đến server"))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is NewsfeedUiState.Error)
        assertEquals("Không thể kết nối đến server", (state as NewsfeedUiState.Error).message)
    }

    @Test
    fun `loadMore appends next page posts to existing list`() = runTest(testDispatcher) {
        val page0Posts = fakePosts(2)
        val page1Posts = listOf(FoodPost("3", "Post 3", 50000.0, 60000.0, 50, 40, null, false, "ACTIVE", null, null, "Shop", null))
        var callCount = 0
        val repo = makeFullFakeRepo { page ->
            callCount++
            if (page == 0) Result.Success(PageResult(page0Posts, 0, hasNext = true))
            else Result.Success(PageResult(page1Posts, 1, hasNext = false))
        }
        val vm = HomeViewModel(GetPostsUseCase(repo, fakeToken), ToggleLikeUseCase(repo, fakeToken), fakeGuestRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as NewsfeedUiState.Success
        assertEquals(3, state.posts.size)
    }

    @Test
    fun `loadMore does nothing when hasNext is false`() = runTest(testDispatcher) {
        var callCount = 0
        val repo = makeFullFakeRepo { page ->
            callCount++
            Result.Success(PageResult(fakePosts(), page, hasNext = false))
        }
        val vm = HomeViewModel(GetPostsUseCase(repo, fakeToken), ToggleLikeUseCase(repo, fakeToken), fakeGuestRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        val countAfterLoad = callCount

        vm.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(countAfterLoad, callCount)
    }

    @Test
    fun `toggleLike updates isLiked optimistically`() = runTest(testDispatcher) {
        val posts = listOf(FoodPost("p1", "Post", 50000.0, 60000.0, 50, 40, null, false, "ACTIVE", null, null, "S", null, likeCount = 5, isLiked = false))
        val vm = makeViewModel(Result.Success(PageResult(posts, 0, false)))
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleLike("p1", isCurrentlyLiked = false)

        val state = vm.uiState.value as NewsfeedUiState.Success
        val updated = state.posts.first { it.postId == "p1" }
        assertTrue(updated.isLiked)
        assertEquals(6, updated.likeCount)
    }
}
