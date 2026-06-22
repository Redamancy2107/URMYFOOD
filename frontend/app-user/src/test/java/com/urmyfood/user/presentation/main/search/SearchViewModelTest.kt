package com.urmyfood.user.presentation.main.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.user.data.model.ShopFollowResponse
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.LikeToggleResult
import com.urmyfood.user.domain.model.PageResult
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.repository.SearchHistoryRepository
import com.urmyfood.user.domain.repository.ShopRepository
import com.urmyfood.user.domain.usecase.AddSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.ClearSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.FollowShopUseCase
import com.urmyfood.user.domain.usecase.GetSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.RemoveSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.SearchPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import com.urmyfood.user.domain.usecase.UnfollowShopUseCase
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

    private fun fakePost(id: String) = FoodPost(
        postId = id,
        dishName = "Post $id",
        price = 50000.0,
        originalPrice = 60000.0,
        maxQuantity = 50,
        remainingQuantity = 40,
        endTime = null,
        isFlashSale = false,
        status = "ACTIVE",
        content = null,
        imageUrl = null,
        shopAccountId = 1L,
        shopName = "Shop",
        shopAvatarUrl = null
    )

    private val fakeToken = object : TokenProvider {
        override fun getAccessToken() = "tok"
    }

    private val fakeShopRepository = object : ShopRepository {
        override suspend fun getProfile(token: String, shopId: Long) = Result.Error("unused")
        override suspend fun getFollowState(token: String, shopId: Long) =
            Result.Success(ShopFollowResponse(shopId, false, 0))
        override suspend fun follow(token: String, shopId: Long) =
            Result.Success(ShopFollowResponse(shopId, true, 1))
        override suspend fun unfollow(token: String, shopId: Long) =
            Result.Success(ShopFollowResponse(shopId, false, 0))
    }

    private class FakeSearchHistoryRepository(initial: List<String> = emptyList()) : SearchHistoryRepository {
        private val items = initial.toMutableList()

        override fun getHistory(): List<String> = items.toList()

        override fun addQuery(query: String): List<String> {
            val normalized = query.trim().replace(Regex("\\s+"), " ")
            if (normalized.isBlank()) return getHistory()
            items.removeAll { it.equals(normalized, ignoreCase = true) }
            items.add(0, normalized)
            while (items.size > 10) {
                items.removeAt(items.lastIndex)
            }
            return getHistory()
        }

        override fun removeQuery(query: String): List<String> {
            items.removeAll { it.equals(query, ignoreCase = true) }
            return getHistory()
        }

        override fun clearHistory() {
            items.clear()
        }
    }

    private fun makeRepo(searchResult: (String, Int) -> Result<PageResult<FoodPost>>): PostRepository =
        object : PostRepository {
            override suspend fun getPosts(token: String?, page: Int, size: Int, anchor: String?) = Result.Success(PageResult<FoodPost>(emptyList(), 0, false))
            override suspend fun searchPosts(token: String?, query: String, page: Int, size: Int, anchor: String?) = searchResult(query, page)
            override suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean, token: String): Result<LikeToggleResult> = Result.Success(LikeToggleResult(0, false))
            override suspend fun getComments(postId: String, token: String, cursor: String?, size: Int) = Result.Success(PageResult<Comment>(emptyList(), 0, false))
            override suspend fun postComment(postId: String, content: String, token: String, parentId: String?) = Result.Success(Comment("", "", null, "", ""))
            override suspend fun getPost(postId: String, token: String?) = Result.Error("Not implemented")
        }

    private fun makeViewModel(
        repo: PostRepository,
        historyRepository: SearchHistoryRepository = FakeSearchHistoryRepository()
    ): SearchViewModel =
        SearchViewModel(
            SearchPostsUseCase(repo, fakeToken),
            ToggleLikeUseCase(repo, fakeToken),
            FollowShopUseCase(fakeShopRepository, fakeToken),
            UnfollowShopUseCase(fakeShopRepository, fakeToken),
            GetSearchHistoryUseCase(historyRepository),
            AddSearchHistoryUseCase(historyRepository),
            RemoveSearchHistoryUseCase(historyRepository),
            ClearSearchHistoryUseCase(historyRepository)
        )

    @Test
    fun `initial state is Idle`() {
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(emptyList(), 0, false)) })
        assertTrue(vm.uiState.value is SearchViewModel.UiState.Idle)
    }

    @Test
    fun `typing query does not search before submit`() = runTest(testDispatcher) {
        var callCount = 0
        val vm = makeViewModel(makeRepo { _, _ ->
            callCount++
            Result.Success(PageResult(emptyList(), 0, false))
        })

        vm.onQueryChanged("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, callCount)
        assertTrue(vm.uiState.value is SearchViewModel.UiState.Idle)
    }

    @Test
    fun `submit with empty query resets to Idle`() = runTest(testDispatcher) {
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(emptyList(), 0, false)) })

        vm.submitSearch("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is SearchViewModel.UiState.Idle)
    }

    @Test
    fun `submit returns Success when repository returns posts`() = runTest(testDispatcher) {
        val posts = listOf(fakePost("1"), fakePost("2"))
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(posts, 0, false)) })

        vm.submitSearch("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SearchViewModel.UiState.Success)
        assertEquals(posts, (state as SearchViewModel.UiState.Success).posts)
    }

    @Test
    fun `submit returns Error when repository returns Error`() = runTest(testDispatcher) {
        val vm = makeViewModel(makeRepo { _, _ -> Result.Error("Server error") })

        vm.submitSearch("xyz")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SearchViewModel.UiState.Error)
        assertEquals("Server error", (state as SearchViewModel.UiState.Error).message)
    }

    @Test
    fun `submit stores recent search newest first`() = runTest(testDispatcher) {
        val history = FakeSearchHistoryRepository(listOf("bun"))
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(emptyList(), 0, false)) }, history)

        vm.submitSearch("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("pho", "bun"), vm.recentSearches.value)
    }

    @Test
    fun `select recent search updates query and submits`() = runTest(testDispatcher) {
        var receivedQuery = ""
        val vm = makeViewModel(makeRepo { query, _ ->
            receivedQuery = query
            Result.Success(PageResult(emptyList(), 0, false))
        })

        vm.selectRecentSearch("bun bo")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("bun bo", vm.query.value)
        assertEquals("bun bo", receivedQuery)
    }

    @Test
    fun `remove and clear recent searches update state`() {
        val history = FakeSearchHistoryRepository(listOf("pho", "bun"))
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(emptyList(), 0, false)) }, history)

        vm.removeRecentSearch("pho")
        assertEquals(listOf("bun"), vm.recentSearches.value)

        vm.clearRecentSearches()
        assertTrue(vm.recentSearches.value!!.isEmpty())
    }

    @Test
    fun `loadMore appends next page when hasNext is true`() = runTest(testDispatcher) {
        val page0 = listOf(fakePost("1"), fakePost("2"))
        val page1 = listOf(fakePost("3"))
        val vm = makeViewModel(makeRepo { _, page ->
            if (page == 0) Result.Success(PageResult(page0, 0, hasNext = true))
            else Result.Success(PageResult(page1, 1, hasNext = false))
        })

        vm.submitSearch("test")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SearchViewModel.UiState.Success
        assertEquals(3, state.posts.size)
    }

    @Test
    fun `submit same query twice does not fire second request`() = runTest(testDispatcher) {
        var callCount = 0
        val vm = makeViewModel(makeRepo { _, _ ->
            callCount++
            Result.Success(PageResult(emptyList(), 0, false))
        })

        vm.submitSearch("pho")
        testDispatcher.scheduler.advanceUntilIdle()
        vm.submitSearch("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, callCount)
    }

    @Test
    fun `editing after success returns to Idle without searching`() = runTest(testDispatcher) {
        var callCount = 0
        val vm = makeViewModel(makeRepo { _, _ ->
            callCount++
            Result.Success(PageResult(listOf(fakePost("1")), 0, false))
        })

        vm.submitSearch("pho")
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onQueryChanged("pho bo")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, callCount)
        assertTrue(vm.uiState.value is SearchViewModel.UiState.Idle)
    }

    @Test
    fun `toggleLike updates result optimistically`() = runTest(testDispatcher) {
        val posts = listOf(
            FoodPost(
                postId = "p1",
                dishName = "Post",
                price = 50000.0,
                originalPrice = 60000.0,
                maxQuantity = 50,
                remainingQuantity = 40,
                endTime = null,
                isFlashSale = false,
                status = "ACTIVE",
                content = null,
                imageUrl = null,
                shopAccountId = 1L,
                shopName = "Shop",
                shopAvatarUrl = null,
                likeCount = 3
            )
        )
        val vm = makeViewModel(makeRepo { _, _ -> Result.Success(PageResult(posts, 0, false)) })
        vm.submitSearch("pho")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleLike("p1", false)

        val state = vm.uiState.value as SearchViewModel.UiState.Success
        val updated = state.posts.first()
        assertTrue(updated.isLiked)
        assertEquals(4, updated.likeCount)
    }
}
