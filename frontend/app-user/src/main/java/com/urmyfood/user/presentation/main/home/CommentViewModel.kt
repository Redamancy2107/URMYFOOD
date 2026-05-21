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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.util.UUID

class CommentViewModel(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val postCommentUseCase: PostCommentUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val comments: List<Comment>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Loading)
    val uiState: LiveData<UiState> = _uiState

    private val _sendError = MutableLiveData<String?>()
    val sendError: LiveData<String?> = _sendError

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = getCommentsUseCase(postId)) {
                is Result.Success -> _uiState.value = UiState.Success(result.data)
                is Result.Error -> _uiState.value = UiState.Error(result.message)
            }
        }
    }

    fun sendComment(postId: String, content: String, optimisticAuthorName: String) {
        if (content.isBlank()) return
        val optimistic = Comment(
            commentId = UUID.randomUUID().toString(),
            authorName = optimisticAuthorName,
            authorAvatarUrl = null,
            content = content,
            createdAt = OffsetDateTime.now().toString()
        )
        val current = (_uiState.value as? UiState.Success)?.comments ?: emptyList()
        _uiState.value = UiState.Success(current + optimistic)

        viewModelScope.launch {
            try {
                when (val result = postCommentUseCase(postId, content)) {
                    is Result.Success -> {
                        val updated = (_uiState.value as? UiState.Success)?.comments
                            ?.map { if (it.commentId == optimistic.commentId) result.data else it }
                            ?: listOf(result.data)
                        _uiState.value = UiState.Success(updated)
                    }
                    is Result.Error -> {
                        val rollback = (_uiState.value as? UiState.Success)?.comments
                            ?.filter { it.commentId != optimistic.commentId }
                            ?: emptyList()
                        _uiState.value = UiState.Success(rollback)
                        _sendError.value = result.message
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
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
