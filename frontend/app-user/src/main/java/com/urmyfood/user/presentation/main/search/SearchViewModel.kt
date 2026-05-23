package com.urmyfood.user.presentation.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.SearchPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchPostsUseCase: SearchPostsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val posts: List<FoodPost>, val hasNext: Boolean, val page: Int) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _likeError = MutableLiveData<String?>()
    val likeError: LiveData<String?> = _likeError

    private val loadedPosts = mutableListOf<FoodPost>()
    private var currentQuery = ""
    private var hasNextPage = false
    private var isLoading = false
    private var debounceJob: Job? = null

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed == currentQuery) return
        currentQuery = trimmed

        debounceJob?.cancel()
        isLoading = false
        if (trimmed.isEmpty()) {
            loadedPosts.clear()
            hasNextPage = false
            _uiState.value = UiState.Idle
            return
        }

        debounceJob = viewModelScope.launch {
            delay(300)
            isLoading = true
            loadedPosts.clear()
            hasNextPage = false
            _uiState.value = UiState.Loading

            try {
                when (val result = searchPostsUseCase(trimmed, page = 0)) {
                    is Result.Success -> {
                        loadedPosts.addAll(result.data.items)
                        hasNextPage = result.data.hasNext
                        _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, 0)
                    }
                    is Result.Error -> _uiState.value = UiState.Error(result.message)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMore() {
        if (!hasNextPage || isLoading || currentQuery.isEmpty()) return
        val currentPageNum = (_uiState.value as? UiState.Success)?.page ?: 0
        isLoading = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            when (val result = searchPostsUseCase(currentQuery, page = currentPageNum + 1)) {
                is Result.Success -> {
                    val newPosts = result.data.items.filter { new ->
                        loadedPosts.none { it.postId == new.postId }
                    }
                    loadedPosts.addAll(newPosts)
                    hasNextPage = result.data.hasNext
                    _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, currentPageNum + 1)
                }
                is Result.Error -> { /* silently ignore */ }
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

    fun clearLikeError() {
        _likeError.value = null
    }

    private fun updatePostLike(postId: String, isLiked: Boolean, likeCount: Int) {
        val index = loadedPosts.indexOfFirst { it.postId == postId }
        if (index >= 0) {
            loadedPosts[index] = loadedPosts[index].copy(isLiked = isLiked, likeCount = likeCount)
            val page = (_uiState.value as? UiState.Success)?.page ?: 0
            _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, page)
        }
    }

    class Factory(
        private val searchPostsUseCase: SearchPostsUseCase,
        private val toggleLikeUseCase: ToggleLikeUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(searchPostsUseCase, toggleLikeUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
