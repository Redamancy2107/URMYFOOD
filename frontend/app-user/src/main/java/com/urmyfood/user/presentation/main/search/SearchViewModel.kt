package com.urmyfood.user.presentation.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.AddSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.ClearSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.FollowShopUseCase
import com.urmyfood.user.domain.usecase.GetSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.RemoveSearchHistoryUseCase
import com.urmyfood.user.domain.usecase.SearchPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import com.urmyfood.user.domain.usecase.UnfollowShopUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchPostsUseCase: SearchPostsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val followShopUseCase: FollowShopUseCase,
    private val unfollowShopUseCase: UnfollowShopUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val addSearchHistoryUseCase: AddSearchHistoryUseCase,
    private val removeSearchHistoryUseCase: RemoveSearchHistoryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
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

    private val _followError = MutableLiveData<String?>()
    val followError: LiveData<String?> = _followError

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val _recentSearches = MutableLiveData<List<String>>(getSearchHistoryUseCase())
    val recentSearches: LiveData<List<String>> = _recentSearches

    private val loadedPosts = mutableListOf<FoodPost>()
    private var currentQuery = ""
    private var typedQuery = ""
    private var hasNextPage = false
    private var isLoading = false
    private var currentAnchor: String? = null
    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        val trimmed = query.trim()
        if (query == typedQuery) return
        typedQuery = query
        _query.value = query

        if (trimmed.isEmpty()) {
            resetSearch()
            currentQuery = ""
            return
        }

        if (trimmed != currentQuery) {
            resetSearch()
        }
    }

    fun submitSearch(query: String = typedQuery) {
        val trimmed = query.trim().replace(Regex("\\s+"), " ")
        if (trimmed.isEmpty()) {
            resetSearch()
            currentQuery = ""
            return
        }

        val alreadyLoaded = trimmed == currentQuery && _uiState.value is UiState.Success
        val alreadyLoading = trimmed == currentQuery && isLoading
        currentQuery = trimmed
        typedQuery = trimmed
        if (_query.value != trimmed) {
            _query.value = trimmed
        }
        _recentSearches.value = addSearchHistoryUseCase(trimmed)
        if (alreadyLoaded || alreadyLoading) {
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            isLoading = true
            loadedPosts.clear()
            hasNextPage = false
            currentAnchor = null
            _uiState.value = UiState.Loading

            try {
                when (val result = searchPostsUseCase(trimmed, page = 0, anchor = null)) {
                    is Result.Success -> {
                        if (currentQuery == trimmed) {
                            loadedPosts.addAll(result.data.items)
                            hasNextPage = result.data.hasNext
                            currentAnchor = result.data.anchor
                            _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, 0)
                        }
                    }
                    is Result.Error -> {
                        if (currentQuery == trimmed) {
                            _uiState.value = UiState.Error(result.message)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                if (currentQuery == trimmed) {
                    isLoading = false
                }
            }
        }
    }

    fun selectRecentSearch(query: String) {
        submitSearch(query)
    }

    fun removeRecentSearch(query: String) {
        _recentSearches.value = removeSearchHistoryUseCase(query)
    }

    fun clearRecentSearches() {
        clearSearchHistoryUseCase()
        _recentSearches.value = emptyList()
    }

    fun loadMore() {
        if (!hasNextPage || isLoading || currentQuery.isEmpty()) return
        val currentPageNum = (_uiState.value as? UiState.Success)?.page ?: 0
        isLoading = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            when (val result = searchPostsUseCase(currentQuery, page = currentPageNum + 1, anchor = currentAnchor)) {
                is Result.Success -> {
                    val newPosts = result.data.items.filter { new ->
                        loadedPosts.none { it.postId == new.postId }
                    }
                    loadedPosts.addAll(newPosts)
                    hasNextPage = result.data.hasNext
                    _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, currentPageNum + 1)
                }
                is Result.Error -> Unit
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

    fun clearFollowError() {
        _followError.value = null
    }

    private fun updatePostLike(postId: String, isLiked: Boolean, likeCount: Int) {
        val index = loadedPosts.indexOfFirst { it.postId == postId }
        if (index >= 0) {
            loadedPosts[index] = loadedPosts[index].copy(isLiked = isLiked, likeCount = likeCount)
            val page = (_uiState.value as? UiState.Success)?.page ?: 0
            _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, page)
        }
    }

    fun toggleFollow(shopId: Long, isCurrentlyFollowing: Boolean) {
        if (shopId <= 0L) return
        updateFollowStateForShop(shopId, !isCurrentlyFollowing)
        com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(shopId, !isCurrentlyFollowing))

        viewModelScope.launch {
            val result = if (isCurrentlyFollowing) {
                unfollowShopUseCase(shopId)
            } else {
                followShopUseCase(shopId)
            }
            when (result) {
                is Result.Success -> {
                    updateFollowStateForShop(result.data.shopId, result.data.isFollowing)
                    com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(
                        Pair(result.data.shopId, result.data.isFollowing)
                    )
                }
                is Result.Error -> {
                    updateFollowStateForShop(shopId, isCurrentlyFollowing)
                    com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(shopId, isCurrentlyFollowing))
                    _followError.value = result.message
                }
            }
        }
    }

    fun updateFollowStateForShop(shopId: Long, isFollowing: Boolean) {
        var changed = false
        for (index in loadedPosts.indices) {
            if (loadedPosts[index].shopAccountId == shopId && loadedPosts[index].isFollowingShop != isFollowing) {
                loadedPosts[index] = loadedPosts[index].copy(isFollowingShop = isFollowing)
                changed = true
            }
        }
        if (changed) {
            val page = (_uiState.value as? UiState.Success)?.page ?: 0
            _uiState.value = UiState.Success(loadedPosts.toList(), hasNextPage, page)
        }
    }

    private fun resetSearch() {
        searchJob?.cancel()
        loadedPosts.clear()
        hasNextPage = false
        isLoading = false
        currentAnchor = null
        _isLoadingMore.value = false
        _uiState.value = UiState.Idle
    }

    class Factory(
        private val searchPostsUseCase: SearchPostsUseCase,
        private val toggleLikeUseCase: ToggleLikeUseCase,
        private val followShopUseCase: FollowShopUseCase,
        private val unfollowShopUseCase: UnfollowShopUseCase,
        private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
        private val addSearchHistoryUseCase: AddSearchHistoryUseCase,
        private val removeSearchHistoryUseCase: RemoveSearchHistoryUseCase,
        private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(
                    searchPostsUseCase,
                    toggleLikeUseCase,
                    followShopUseCase,
                    unfollowShopUseCase,
                    getSearchHistoryUseCase,
                    addSearchHistoryUseCase,
                    removeSearchHistoryUseCase,
                    clearSearchHistoryUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
