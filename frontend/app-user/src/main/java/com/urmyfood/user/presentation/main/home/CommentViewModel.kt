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

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _sendError = MutableLiveData<String?>()
    val sendError: LiveData<String?> = _sendError

    private val loadedComments = mutableListOf<Comment>()
    private var currentPostId: String? = null
    private var currentPage = 0
    private var hasNextPage = false
    private var isLoading = false

    fun loadComments(postId: String) {
        if (isLoading) return
        currentPostId = postId
        isLoading = true
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                when (val result = getCommentsUseCase(postId, 0, PAGE_SIZE)) {
                    is Result.Success -> {
                        loadedComments.clear()
                        loadedComments.addAll(result.data.items)
                        currentPage = result.data.page
                        hasNextPage = result.data.hasNext
                        _uiState.value = UiState.Success(loadedComments.toList())
                    }
                    is Result.Error -> _uiState.value = UiState.Error(result.message)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMore() {
        val postId = currentPostId ?: return
        if (!hasNextPage || isLoading) return
        isLoading = true
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                when (val result = getCommentsUseCase(postId, currentPage + 1, PAGE_SIZE)) {
                    is Result.Success -> {
                        val existingIds = loadedComments.mapTo(HashSet()) { it.commentId }
                        loadedComments.addAll(result.data.items.filterNot { it.commentId in existingIds })
                        currentPage = result.data.page
                        hasNextPage = result.data.hasNext
                        _uiState.value = UiState.Success(loadedComments.toList())
                    }
                    is Result.Error -> _sendError.value = result.message
                }
            } finally {
                _isLoadingMore.value = false
                isLoading = false
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
        loadedComments.add(0, optimistic)
        _uiState.value = UiState.Success(loadedComments.toList())

        viewModelScope.launch {
            try {
                when (val result = postCommentUseCase(postId, content)) {
                    is Result.Success -> {
                        val index = loadedComments.indexOfFirst { it.commentId == optimistic.commentId }
                        if (index >= 0) {
                            loadedComments[index] = result.data
                            _uiState.value = UiState.Success(loadedComments.toList())
                        }
                    }
                    is Result.Error -> {
                        loadedComments.removeAll { it.commentId == optimistic.commentId }
                        _uiState.value = UiState.Success(loadedComments.toList())
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

    companion object {
        private const val PAGE_SIZE = 20
    }
}
