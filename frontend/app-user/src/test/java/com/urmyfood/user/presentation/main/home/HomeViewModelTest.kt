package com.urmyfood.user.presentation.main.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.usecase.GetPostsUseCase
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

    // Swaps out the Architecture Components background executor for a synchronous one.
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

    // ---------------------------------------------------------------------------
    // Fakes
    // ---------------------------------------------------------------------------

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

    private fun makeViewModel(result: Result<List<FoodPost>>): HomeViewModel {
        val fakeRepository = object : PostRepository {
            override suspend fun getPosts(): Result<List<FoodPost>> = result
        }
        return HomeViewModel(GetPostsUseCase(fakeRepository))
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    /**
     * Right after construction the ViewModel calls loadPosts() inside init{}.
     * Before the coroutine executes the first emission must still be Loading.
     */
    @Test
    fun `initial uiState is Loading before coroutine executes`() {
        val fakeRepository = object : PostRepository {
            override suspend fun getPosts(): Result<List<FoodPost>> =
                Result.Success(emptyList())
        }
        // Do NOT advance the dispatcher — capture state before coroutine runs.
        val viewModel = HomeViewModel(GetPostsUseCase(fakeRepository))
        assertEquals(NewsfeedUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadPosts emits Success with posts when repository returns Success`() = runTest(testDispatcher) {
        val posts = fakePosts()
        val viewModel = makeViewModel(Result.Success(posts))

        // Advance coroutines so the suspended call completes.
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
        // Use a deferred to pause the repository response so we can observe the Loading state.
        val deferred = CompletableDeferred<Result<List<FoodPost>>>()
        val blockingRepository = object : PostRepository {
            override suspend fun getPosts(): Result<List<FoodPost>> = deferred.await()
        }
        val viewModel = HomeViewModel(GetPostsUseCase(blockingRepository))

        // State is Loading right away (init coroutine hasn't finished — deferred is blocking).
        testDispatcher.scheduler.runCurrent()
        assertEquals(NewsfeedUiState.Loading, viewModel.uiState.value)

        // Release the deferred so the coroutine can complete.
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
