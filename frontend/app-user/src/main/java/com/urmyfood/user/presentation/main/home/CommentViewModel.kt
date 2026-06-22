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

    // Raw flat list from the API — not yet threaded
    private val loadedComments = mutableListOf<Comment>()
    private var nextCursor: String? = null
    private var hasNextPage = false
    private var isLoading = false

    fun loadComments(postId: String) {
        if (isLoading) return
        isLoading = true
        loadedComments.clear()
        nextCursor = null
        hasNextPage = false
        _uiState.value = CommentUiState.Loading
        viewModelScope.launch {
            when (val r = getCommentsUseCase(postId, cursor = null)) {
                is Result.Success -> {
                    loadedComments.addAll(r.data.items)
                    hasNextPage = r.data.hasNext
                    nextCursor = r.data.nextCursor
                    emitThreaded()
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
            when (val r = getCommentsUseCase(postId, cursor = nextCursor)) {
                is Result.Success -> {
                    loadedComments.addAll(r.data.items)
                    hasNextPage = r.data.hasNext
                    nextCursor = r.data.nextCursor
                    emitThreaded()
                }
                is Result.Error -> {}
            }
            isLoading = false
            _isLoadingMore.value = false
        }
    }

    fun postComment(postId: String, content: String, parentId: String? = null) {
        viewModelScope.launch {
            when (val r = postCommentUseCase(postId, content, parentId)) {
                is Result.Success -> {
                    loadedComments.add(r.data)
                    emitThreaded()
                    com.urmyfood.user.di.ServiceLocator.postCommentEvent.postValue(postId)
                    _sendResult.value = null
                }
                is Result.Error -> _sendResult.value = r.message
            }
        }
    }

    /**
     * Transforms the flat [loadedComments] list into a threaded display list:
     *   Parent 1 (newest first)
     *     └─ Reply A (oldest first)
     *     └─ Reply B
     *   Parent 2
     *     └─ Reply C
     *   ...
     */
    private fun emitThreaded() {
        _uiState.value = CommentUiState.Success(buildThreadedList(loadedComments))
    }

    private fun buildThreadedList(flat: List<Comment>): List<Comment> {
        // Separate parents from replies
        val parents = flat.filter { it.parentId == null }
        val repliesByParent = flat.filter { it.parentId != null }
            .groupBy { it.parentId!! }

        // Parents: newest first (createdAt is already formatted as "HH:mm dd/MM/yyyy",
        // so we keep the original order from the API which is created_at DESC)
        val result = mutableListOf<Comment>()
        for (parent in parents) {
            result.add(parent)
            // Replies under this parent: oldest first (chronological reading order)
            val replies = repliesByParent[parent.commentId] ?: emptyList()
            result.addAll(replies.reversed())
        }

        // Orphaned replies whose parent isn't in the current page — show at the end
        val knownParentIds = parents.map { it.commentId }.toSet()
        val orphans = flat.filter { it.parentId != null && it.parentId !in knownParentIds }
        result.addAll(orphans)

        return result
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
