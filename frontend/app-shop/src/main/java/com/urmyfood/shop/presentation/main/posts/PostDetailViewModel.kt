package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.usecase.DeletePostUseCase
import com.urmyfood.shop.domain.usecase.GetPostByIdUseCase
import com.urmyfood.shop.domain.usecase.TogglePostStatusUseCase
import com.urmyfood.shop.domain.usecase.UpdateRemainingQuantityUseCase
import kotlinx.coroutines.launch

sealed class PostDetailUiState {
    object Loading : PostDetailUiState()
    data class Success(val post: Post) : PostDetailUiState()
    data class Error(val message: String) : PostDetailUiState()
}

class PostDetailViewModel(
    private val postId: String,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val togglePostStatusUseCase: TogglePostStatusUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val updateRemainingQuantityUseCase: UpdateRemainingQuantityUseCase
) : ViewModel() {

    private val _quantityError = MutableLiveData<String?>()
    val quantityError: LiveData<String?> = _quantityError

    private val _uiState = MutableLiveData<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: LiveData<PostDetailUiState> = _uiState

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    init {
        loadPost()
    }

    fun loadPost() {
        _uiState.value = PostDetailUiState.Loading
        viewModelScope.launch {
            when (val result = getPostByIdUseCase(postId)) {
                is Result.Success -> _uiState.value = PostDetailUiState.Success(result.data)
                is Result.Error -> _uiState.value = PostDetailUiState.Error(result.message)
            }
        }
    }

    fun toggleStatus() {
        val current = (_uiState.value as? PostDetailUiState.Success)?.post ?: return
        viewModelScope.launch {
            when (val result = togglePostStatusUseCase(postId, !current.isActive)) {
                is Result.Success -> _uiState.value = PostDetailUiState.Success(result.data)
                is Result.Error -> _uiState.value = PostDetailUiState.Error(result.message)
            }
        }
    }

    fun updateRemainingQuantity(newQuantity: Int) {
        val current = (_uiState.value as? PostDetailUiState.Success)?.post ?: return
        val clamped = newQuantity.coerceIn(0, current.maxQuantity)
        viewModelScope.launch {
            when (val result = updateRemainingQuantityUseCase(postId, clamped)) {
                is Result.Success -> _uiState.value = PostDetailUiState.Success(result.data)
                is Result.Error -> _quantityError.value = result.message
            }
        }
    }

    fun clearQuantityError() {
        _quantityError.value = null
    }

    fun deletePost() {
        viewModelScope.launch {
            _deleteResult.value = deletePostUseCase(postId)
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    class Factory(
        private val postId: String,
        private val getPostByIdUseCase: GetPostByIdUseCase,
        private val togglePostStatusUseCase: TogglePostStatusUseCase,
        private val deletePostUseCase: DeletePostUseCase,
        private val updateRemainingQuantityUseCase: UpdateRemainingQuantityUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
                return PostDetailViewModel(
                    postId, getPostByIdUseCase, togglePostStatusUseCase, deletePostUseCase,
                    updateRemainingQuantityUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
