package com.urmyfood.user.presentation.main.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.FollowShopUseCase
import com.urmyfood.user.domain.usecase.GetSavedPostsUseCase
import com.urmyfood.user.domain.usecase.SavePostUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import com.urmyfood.user.domain.usecase.UnfollowShopUseCase
import com.urmyfood.user.domain.usecase.UnsavePostUseCase
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val posts: List<FoodPost>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(
    private val getSavedPostsUseCase: GetSavedPostsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val followShopUseCase: FollowShopUseCase,
    private val unfollowShopUseCase: UnfollowShopUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unsavePostUseCase: UnsavePostUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: LiveData<FavoritesUiState> = _uiState

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val posts = mutableListOf<FoodPost>()

    fun loadSavedPosts() {
        _uiState.value = FavoritesUiState.Loading
        viewModelScope.launch {
            when (val result = getSavedPostsUseCase()) {
                is Result.Success -> {
                    posts.clear()
                    posts.addAll(result.data.items)
                    emit()
                }
                is Result.Error -> _uiState.value = FavoritesUiState.Error(result.message)
            }
        }
    }

    fun toggleLike(post: FoodPost) {
        val currentCount = post.likeCount
        val nextCount = if (post.isLiked) (currentCount - 1).coerceAtLeast(0) else currentCount + 1
        updateLike(post.postId, !post.isLiked, nextCount)
        com.urmyfood.user.di.ServiceLocator.postLikeEvent.postValue(Pair(post.postId, Pair(!post.isLiked, nextCount)))

        viewModelScope.launch {
            when (val result = toggleLikeUseCase(post.postId, post.isLiked)) {
                is Result.Success -> {
                    updateLike(post.postId, result.data.isLiked, result.data.likeCount)
                    com.urmyfood.user.di.ServiceLocator.postLikeEvent.postValue(Pair(post.postId, Pair(result.data.isLiked, result.data.likeCount)))
                }
                is Result.Error -> {
                    updateLike(post.postId, post.isLiked, currentCount)
                    com.urmyfood.user.di.ServiceLocator.postLikeEvent.postValue(Pair(post.postId, Pair(post.isLiked, currentCount)))
                    _message.value = result.message
                }
            }
        }
    }

    fun toggleFollow(post: FoodPost) {
        val shopId = post.shopAccountId
        if (shopId <= 0L) return
        val previous = post.isFollowingShop
        updateFollow(shopId, !previous)
        com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(shopId, !previous))

        viewModelScope.launch {
            val result = if (previous) unfollowShopUseCase(shopId) else followShopUseCase(shopId)
            when (result) {
                is Result.Success -> {
                    updateFollow(result.data.shopId, result.data.isFollowing)
                    com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(result.data.shopId, result.data.isFollowing))
                }
                is Result.Error -> {
                    updateFollow(shopId, previous)
                    com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(shopId, previous))
                    _message.value = result.message
                }
            }
        }
    }

    fun toggleSave(post: FoodPost) {
        updateSaved(post.postId, !post.isSaved)
        com.urmyfood.user.di.ServiceLocator.postSavedEvent.postValue(Pair(post.postId, !post.isSaved))

        viewModelScope.launch {
            val result = if (post.isSaved) unsavePostUseCase(post.postId) else savePostUseCase(post.postId)
            when (result) {
                is Result.Success -> {
                    updateSaved(result.data.postId, result.data.isSaved)
                    com.urmyfood.user.di.ServiceLocator.postSavedEvent.postValue(Pair(result.data.postId, result.data.isSaved))
                }
                is Result.Error -> {
                    updateSaved(post.postId, post.isSaved)
                    com.urmyfood.user.di.ServiceLocator.postSavedEvent.postValue(Pair(post.postId, post.isSaved))
                    _message.value = result.message
                }
            }
        }
    }

    fun incrementComment(postId: String) {
        val index = posts.indexOfFirst { it.postId == postId }
        if (index >= 0) {
            posts[index] = posts[index].copy(commentCount = posts[index].commentCount + 1)
            emit()
        }
    }

    fun updateLike(postId: String, isLiked: Boolean, likeCount: Int) {
        val index = posts.indexOfFirst { it.postId == postId }
        if (index >= 0) {
            posts[index] = posts[index].copy(isLiked = isLiked, likeCount = likeCount)
            emit()
        }
    }

    fun updateFollow(shopId: Long, isFollowing: Boolean) {
        var changed = false
        for (index in posts.indices) {
            if (posts[index].shopAccountId == shopId && posts[index].isFollowingShop != isFollowing) {
                posts[index] = posts[index].copy(isFollowingShop = isFollowing)
                changed = true
            }
        }
        if (changed) emit()
    }

    fun updateSaved(postId: String, isSaved: Boolean) {
        val index = posts.indexOfFirst { it.postId == postId }
        if (index >= 0) {
            if (isSaved) {
                posts[index] = posts[index].copy(isSaved = true)
            } else {
                posts.removeAt(index)
            }
            emit()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun emit() {
        _uiState.value = FavoritesUiState.Success(posts.toList())
    }

    class Factory(
        private val getSavedPostsUseCase: GetSavedPostsUseCase,
        private val toggleLikeUseCase: ToggleLikeUseCase,
        private val followShopUseCase: FollowShopUseCase,
        private val unfollowShopUseCase: UnfollowShopUseCase,
        private val savePostUseCase: SavePostUseCase,
        private val unsavePostUseCase: UnsavePostUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                return FavoritesViewModel(
                    getSavedPostsUseCase,
                    toggleLikeUseCase,
                    followShopUseCase,
                    unfollowShopUseCase,
                    savePostUseCase,
                    unsavePostUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
