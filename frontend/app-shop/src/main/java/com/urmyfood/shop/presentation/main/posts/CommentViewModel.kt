package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Comment
import com.urmyfood.shop.domain.usecase.AddCommentUseCase
import com.urmyfood.shop.domain.usecase.GetPostCommentsUseCase
import kotlinx.coroutines.launch

sealed class CommentUiState {
    object Loading : CommentUiState()
    data class Success(val comments: List<Comment>) : CommentUiState()
    data class Error(val message: String) : CommentUiState()
}

class CommentViewModel(
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<CommentUiState>(CommentUiState.Loading)
    val uiState: LiveData<CommentUiState> = _uiState

    private val _sendResult = MutableLiveData<String?>()
    val sendResult: LiveData<String?> = _sendResult

    private var currentPostId: String = ""
    private val loadedComments = mutableListOf<Comment>()

    fun loadComments(postId: String) {
        currentPostId = postId
        _uiState.value = CommentUiState.Loading
        viewModelScope.launch {
            when (val result = getPostCommentsUseCase(postId)) {
                is Result.Success -> {
                    loadedComments.clear()
                    loadedComments.addAll(result.data)
                    emitThreaded()
                }
                is Result.Error -> _uiState.value = CommentUiState.Error(result.message)
            }
        }
    }

    fun postComment(postId: String, content: String, parentId: String? = null) {
        viewModelScope.launch {
            when (val result = addCommentUseCase(postId, content, parentId)) {
                is Result.Success -> {
                    loadedComments.add(result.data)
                    emitThreaded()
                    _sendResult.value = null
                }
                is Result.Error -> _uiState.value = CommentUiState.Error(result.message)
            }
        }
    }

    private fun emitThreaded() {
        _uiState.value = CommentUiState.Success(buildThreadedList(loadedComments))
    }

    private fun buildThreadedList(flat: List<Comment>): List<Comment> {
        val parents = flat.filter { it.parentId == null }
        val repliesByParent = flat.filter { it.parentId != null }.groupBy { it.parentId!! }

        val result = mutableListOf<Comment>()
        for (parent in parents) {
            result.add(parent)
            result.addAll((repliesByParent[parent.commentId] ?: emptyList()).reversed())
        }

        val knownParentIds = parents.map { it.commentId }.toSet()
        result.addAll(flat.filter { it.parentId != null && it.parentId !in knownParentIds })
        return result
    }

    fun clearSendResult() {
        _sendResult.value = "CLEARED"
    }

    class Factory(
        private val getPostCommentsUseCase: GetPostCommentsUseCase,
        private val addCommentUseCase: AddCommentUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
                return CommentViewModel(getPostCommentsUseCase, addCommentUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
