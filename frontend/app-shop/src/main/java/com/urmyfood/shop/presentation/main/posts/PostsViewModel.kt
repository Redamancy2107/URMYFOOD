package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.usecase.DeletePostUseCase
import com.urmyfood.shop.domain.usecase.GetMyPostsUseCase
import com.urmyfood.shop.domain.usecase.TogglePostStatusUseCase
import kotlinx.coroutines.launch

sealed class PostsUiState {
    object Loading : PostsUiState()
    data class Success(val posts: List<Post>) : PostsUiState()
    data class Error(val message: String) : PostsUiState()
}

class PostsViewModel(
    private val getMyPostsUseCase: GetMyPostsUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val togglePostStatusUseCase: TogglePostStatusUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<PostsUiState>(PostsUiState.Loading)
    val uiState: LiveData<PostsUiState> = _uiState

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private var allPosts: List<Post> = emptyList()
    private var currentQuery: String = ""

    init {
        loadPosts()
    }

    fun loadPosts() {
        _uiState.value = PostsUiState.Loading
        viewModelScope.launch {
            when (val result = getMyPostsUseCase()) {
                is Result.Success -> {
                    allPosts = result.data
                    applyFilter()
                }
                is Result.Error -> _uiState.value = PostsUiState.Error(result.message)
            }
        }
    }

    fun filterPosts(query: String) {
        currentQuery = query
        applyFilter()
    }

    fun toggleActive(postId: String, isActive: Boolean) {
        viewModelScope.launch {
            when (val result = togglePostStatusUseCase(postId, isActive)) {
                is Result.Success -> {
                    allPosts = allPosts.map { if (it.postId == postId) result.data else it }
                    applyFilter()
                }
                is Result.Error -> _uiState.value = PostsUiState.Error(result.message)
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = deletePostUseCase(postId)
            _deleteResult.value = result
            if (result is Result.Success) {
                allPosts = allPosts.filter { it.postId != postId }
                applyFilter()
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    private fun applyFilter() {
        val filtered = if (currentQuery.isBlank()) {
            allPosts
        } else {
            allPosts.filter { it.dishName.contains(currentQuery, ignoreCase = true) }
        }
        _uiState.value = PostsUiState.Success(filtered)
    }

    class Factory(
        private val getMyPostsUseCase: GetMyPostsUseCase,
        private val deletePostUseCase: DeletePostUseCase,
        private val togglePostStatusUseCase: TogglePostStatusUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostsViewModel::class.java)) {
                return PostsViewModel(getMyPostsUseCase, deletePostUseCase, togglePostStatusUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
