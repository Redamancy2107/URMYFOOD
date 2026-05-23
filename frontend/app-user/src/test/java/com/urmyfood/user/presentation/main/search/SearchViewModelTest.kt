package com.urmyfood.user.presentation.main.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.usecase.SearchPostsUseCase
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
class SearchViewModelTest {

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

    private fun fakePost(id: String) = FoodPost(id, "Post $id", 50000.0, 60000.0, 50, 40, null, false, "ACTIVE", null, null, "Shop", null)

    private val fakeToken = object : TokenProvider {
        override fun getAccessToken() = "tok"
    }

    private fun makeRepo(searchResult: (String, Int) -> Result<PageResult<FoodPost>>): PostRepository =
        object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun searchPosts(token: String?, query: String, page: Int, size: Int) = searchResult(query, page)
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, page: Int, size: Int) = Result.Success(PageResult<Comment>(emptyList(), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String) = Result.Success(Comment("", "", null, "", ""))
        }

    private fun makeViewModel(repo: PostRepository): SearchViewModel =
        SearchViewModel(SearchPostsUseCase(repo, fakeToken), ToggleLikeUseCase(repo, fakeToken))

    @Test
    fun `initial state is Idle`() {
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(emptyList(), 0, false)) })
        assertTrue(vm.uiState.value is SearchViewModel.UiState.Idle)
    }

    @Test
    fun `search with empty query resets to Idle`() = runTest(testDispatcher) {
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(emptyList(), 0, false)) })
        vm.search("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.search("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is SearchViewModel.UiState.Idle)
    }

    @Test
    fun `search returns Success when repository returns posts`() = runTest(testDispatcher) {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(posts, 0, false)) })

        vm.search("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SearchViewModel.UiState.Success)
        assertEquals(posts, (state as SearchViewModel.UiState.Success).posts)
    }

    @Test
    fun `search returns Error when repository returns Error`() = runTest(testDispatcher) {
        val vm = makeViewModel(makeRepo { _, _ -> Result.Error("Server error") })

        vm.search("xyz")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SearchViewModel.UiState.Error)
        assertEquals("Server error", (state as SearchViewModel.UiState.Error).message)
    }

    @Test
    fun `loadMore appends next page when hasNext is true`() = runTest(testDispatcher) {
        val page0 = listOf(fakePost("1"), fakePost("2"))
        val page1 = listOf(fakePost("3"))
        val vm = makeViewModel(makeRepo { _, page ->
            if (page == 0) Result.Success(PageResult(page0, 0, hasNext = true))
            else Result.Success(PageResult(page1, 1, hasNext = false))
        })

        vm.search("test")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SearchViewModel.UiState.Success
        assertEquals(3, state.posts.size)
    }

    @Test
    fun `search same query twice does not fire second request`() = runTest(testDispatcher) {
        var callCount = 0
        val vm = makeViewModel(makeRepo { _, _ ->
            callCount++
            Result.Success(PageResult(emptyList(), 0, false))
        })

        vm.search("pho")
        testDispatcher.scheduler.advanceUntilIdle()
        vm.search("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, callCount)
    }

    @Test
    fun `toggleLike updates result optimistically`() = runTest(testDispatcher) {
        val posts = listOf(FoodPost("p1", "Post", 50000.0, 60000.0, 50, 40, null, false, "ACTIVE", null, null, "Shop", null, likeCount = 3))
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(posts, 0, false)) })
        vm.search("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleLike("p1", false)

        val state = vm.uiState.value as SearchViewModel.UiState.Success
        val updated = state.posts.first()
        assertTrue(updated.isLiked)
        assertEquals(4, updated.likeCount)
    }
}
