package com.urmyfood.user.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.Comment
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.GetCommentsUseCase
import com.urmyfood.user.domain.usecase.PostCommentUseCase
import kotlinx.coroutines.launch

sealed class CommentUiState {
    object Loading : CommentUiState()
    data class Success(val comments: List<Comment>) : CommentUiState()
    data class Error(val message: String) : CommentUiState()
}

class CommentViewModel(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val postCommentUseCase: PostCommentUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<CommentUiState>(CommentUiState.Loading)
    val uiState: LiveData<CommentUiState> = _uiState

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _sendResult = MutableLiveData<String?>()
    val sendResult: LiveData<String?> = _sendResult

    private val loadedComments = mutableListOf<Comment>()
    private var currentPage = 0
    private var hasNextPage = false
    private var isLoading = false

    fun loadComments(postId: String) {
        if (isLoading) return
        isLoading = true
        loadedComments.clear()
        currentPage = 0
        _uiState.value = CommentUiState.Loading
        viewModelScope.launch {
            when (val r = getCommentsUseCase(postId, 0)) {
                is Result.Success -> {
                    loadedComments.addAll(r.data.items)
                    hasNextPage = r.data.hasNext
                    _uiState.value = CommentUiState.Success(loadedComments.toList())
                }
                is Result.Error -> _uiState.value = CommentUiState.Error(r.message)
            }
            isLoading = false
        }
    }

    fun loadMore(postId: String) {
        if (!hasNextPage || isLoading) return
        isLoading = true
        _isLoadingMore.value = true
        viewModelScope.launch {
            when (val r = getCommentsUseCase(postId, currentPage + 1)) {
                is Result.Success -> {
                    loadedComments.addAll(r.data.items)
                    hasNextPage = r.data.hasNext
                    currentPage++
                    _uiState.value = CommentUiState.Success(loadedComments.toList())
                }
                is Result.Error -> {}
            }
            isLoading = false
            _isLoadingMore.value = false
        }
    }

    fun postComment(postId: String, content: String) {
        viewModelScope.launch {
            when (val r = postCommentUseCase(postId, content)) {
                is Result.Success -> {
                    loadedComments.add(0, r.data)
                    _uiState.value = CommentUiState.Success(loadedComments.toList())
                    _sendResult.value = null
                }
                is Result.Error -> _sendResult.value = r.message
            }
        }
    }

    fun clearSendResult() {
        _sendResult.value = "CLEARED"
    }

    class Factory(
        private val getCommentsUseCase: GetCommentsUseCase,
        private val postCommentUseCase: PostCommentUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CommentViewModel(getCommentsUseCase, postCommentUseCase) as T
    }
}
