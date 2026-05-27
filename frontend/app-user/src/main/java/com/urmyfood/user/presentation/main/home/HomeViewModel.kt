package com.urmyfood.user.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.GuestRepository
import com.urmyfood.user.domain.usecase.GetPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed class NewsfeedUiState {
    object Loading : NewsfeedUiState()
    data class Success(val posts: List<FoodPost>) : NewsfeedUiState()
    data class Error(val message: String) : NewsfeedUiState()
}

class HomeViewModel(
    private val getPostsUseCase: GetPostsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val guestRepository: GuestRepository
) : ViewModel() {

    val isGuest: Boolean get() = guestRepository.isGuest()

    private val _uiState = MutableLiveData<NewsfeedUiState>(NewsfeedUiState.Loading)
    val uiState: LiveData<NewsfeedUiState> = _uiState

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _likeError = MutableLiveData<String?>()
    val likeError: LiveData<String?> = _likeError

    private val _sortOrder = MutableLiveData<String?>(null)
    val sortOrder: LiveData<String?> = _sortOrder

    private val loadedPosts = mutableListOf<FoodPost>()
    private var currentPage = 0
    private var hasNextPage = false
    private var isLoading = false

    init {
        loadPosts()
    }

    fun setSortOrder(order: String?) {
        _sortOrder.value = order
        applySortingAndEmit()
    }

    private fun applySortingAndEmit() {
        val sortedList = when (_sortOrder.value) {
            "LOW_TO_HIGH" -> loadedPosts.sortedBy { it.price }
            "HIGH_TO_LOW" -> loadedPosts.sortedByDescending { it.price }
            else -> loadedPosts.toList()
        }
        _uiState.value = NewsfeedUiState.Success(sortedList)
    }

    fun loadPosts() {
        if (isLoading) return
        isLoading = true
        loadedPosts.clear()
        currentPage = 0
        hasNextPage = false
        _uiState.value = NewsfeedUiState.Loading

        viewModelScope.launch {
            when (val result = getPostsUseCase(page = 0)) {
                is Result.Success -> {
                    loadedPosts.addAll(result.data.items)
                    hasNextPage = result.data.hasNext
                    applySortingAndEmit()
                }
                is Result.Error -> _uiState.value = NewsfeedUiState.Error(result.message)
            }
            isLoading = false
        }
    }

    fun loadMore() {
        if (!hasNextPage || isLoading) return
        isLoading = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            when (val result = getPostsUseCase(page = currentPage + 1)) {
                is Result.Success -> {
                    val newPosts = result.data.items.filter { new ->
                        loadedPosts.none { it.postId == new.postId }
                    }
                    loadedPosts.addAll(newPosts)
                    hasNextPage = result.data.hasNext
                    currentPage++
                    applySortingAndEmit()
                }
                is Result.Error -> { /* silently ignore load-more errors */ }
            }
            isLoading = false
            _isLoadingMore.value = false
        }
    }

    fun toggleLike(postId: String, isCurrentlyLiked: Boolean) {
        val currentCount = loadedPosts.find { it.postId == postId }?.likeCount ?: 0
        val optimisticCount = if (isCurrentlyLiked) (currentCount - 1).coerceAtLeast(0) else currentCount + 1
        updatePostLike(postId, !isCurrentlyLiked, optimisticCount)

        viewModelScope.launch {
            try {
                when (val result = toggleLikeUseCase(postId, isCurrentlyLiked)) {
                    is Result.Success -> updatePostLike(postId, result.data.isLiked, result.data.likeCount)
                    is Result.Error -> {
                        updatePostLike(postId, isCurrentlyLiked, currentCount)
                        _likeError.value = result.message
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    fun clearLikeError() { _likeError.value = null }

    private fun updatePostLike(postId: String, isLiked: Boolean, likeCount: Int) {
        val idx = loadedPosts.indexOfFirst { it.postId == postId }
        if (idx >= 0) {
            loadedPosts[idx] = loadedPosts[idx].copy(isLiked = isLiked, likeCount = likeCount)
            applySortingAndEmit()
        }
    }

    class Factory(
        private val getPostsUseCase: GetPostsUseCase,
        private val toggleLikeUseCase: ToggleLikeUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(
                    getPostsUseCase,
                    toggleLikeUseCase,
                    com.urmyfood.user.di.ServiceLocator.guestSessionManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
