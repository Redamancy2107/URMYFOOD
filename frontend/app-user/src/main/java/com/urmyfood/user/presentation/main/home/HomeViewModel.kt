package com.urmyfood.user.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.GetPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed class NewsfeedUiState {
    object Loading : NewsfeedUiState()
    data class Success(val posts: List<FoodPost>) : NewsfeedUiState()
    data class Error(val message: String) : NewsfeedUiState()
}

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel(
    private val getPostsUseCase: GetPostsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val guestRepository: com.urmyfood.user.domain.repository.GuestRepository
) : ViewModel() {
    
    val isGuest: Boolean get() = guestRepository.isGuest()
    
    private val _uiState = MutableLiveData<NewsfeedUiState>(NewsfeedUiState.Loading)
    val uiState: LiveData<NewsfeedUiState> = _uiState

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = NewsfeedUiState.Loading
            when (val result = getPostsUseCase()) {
                is Result.Success -> _uiState.value = NewsfeedUiState.Success(result.data)
                is Result.Error -> _uiState.value = NewsfeedUiState.Error(result.message)
            }
        }
    }

    fun toggleLike(postId: String, isCurrentlyLiked: Boolean) {
        val currentCount = currentPosts().find { it.postId == postId }?.likeCount ?: 0
        val optimisticCount = if (isCurrentlyLiked) currentCount - 1 else currentCount + 1
        updatePostLike(postId, !isCurrentlyLiked, optimisticCount)
        viewModelScope.launch {
            try {
                when (val result = toggleLikeUseCase(postId, isCurrentlyLiked)) {
                    is Result.Success -> updatePostLike(postId, result.data.isLiked, result.data.likeCount)
                    is Result.Error -> updatePostLike(postId, isCurrentlyLiked, currentCount)
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun currentPosts() = (_uiState.value as? NewsfeedUiState.Success)?.posts ?: emptyList()

    private fun updatePostLike(postId: String, isLiked: Boolean, likeCount: Int) {
        _uiState.value = NewsfeedUiState.Success(currentPosts().map {
            if (it.postId == postId) it.copy(isLiked = isLiked, likeCount = likeCount) else it
        })
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
