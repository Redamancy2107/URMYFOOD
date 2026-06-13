package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.urmyfood.shop.domain.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CommentUiState {
    object Loading : CommentUiState()
    data class Success(val comments: List<Comment>) : CommentUiState()
    data class Error(val message: String) : CommentUiState()
}

class CommentViewModel : ViewModel() {

    private val _uiState = MutableLiveData<CommentUiState>(CommentUiState.Loading)
    val uiState: LiveData<CommentUiState> = _uiState

    private val _sendResult = MutableLiveData<String?>()
    val sendResult: LiveData<String?> = _sendResult

    // Local static/memory storage for comments of different posts so they persist during session
    companion object {
        val mockCommentsMap = mutableMapOf<String, MutableList<Comment>>().apply {
            // post_1 comments
            put("post_1", mutableListOf(
                Comment("c_1_1", "Linh Nguyễn", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "Nước lèo ngon đậm đà lắm shop ơi!", "10 phút trước"),
                Comment("c_1_2", "Minh Hoàng", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "Thịt bò nạm mềm ngọt cực kỳ, sẽ ủng hộ tiếp", "5 phút trước"),
                Comment("c_1_3", "Tuấn Trần", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100", "Có bán thêm chả cua không ạ?", "2 phút trước")
            ))
            // post_2 comments
            put("post_2", mutableListOf(
                Comment("c_2_1", "Anh Thư", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "Sườn nướng sốt Teriyaki siêu thơm, vị vừa ăn", "15 phút trước"),
                Comment("c_2_2", "Quốc Bảo", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100", "Cơm tấm dẻo ngon, dưa chua ăn kèm đỡ ngấy lắm", "8 phút trước")
            ))
            // post_3 comments
            put("post_3", mutableListOf(
                Comment("c_3_1", "Vy Vy", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100", "Trà sữa đậm vị béo ngậy, trân châu dai giòn xuất sắc!", "20 phút trước"),
                Comment("c_3_2", "Hải Nam", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=100", "Độ ngọt vừa phải, không bị ngọt gắt, 10 điểm", "12 phút trước")
            ))
            // post_4 comments
            put("post_4", mutableListOf(
                Comment("c_4_1", "Duy Mạnh", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=100", "Bánh mì giòn rụm, đầy đặn nhân luôn", "30 phút trước")
            ))
            // post_5 comments
            put("post_5", mutableListOf(
                Comment("c_5_1", "Hương Giang", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=100", "Gỏi cuốn tôm thịt tươi ngon, nước chấm tương đậu phộng đỉnh chóp", "25 phút trước")
            ))
        }
    }

    private var currentPostId: String = ""
    private val loadedComments = mutableListOf<Comment>()

    fun loadComments(postId: String) {
        currentPostId = postId
        loadedComments.clear()
        val existing = mockCommentsMap[postId] ?: mutableListOf()
        loadedComments.addAll(existing)
        emitThreaded()
    }

    fun postComment(postId: String, content: String, parentId: String? = null) {
        val newCommentId = "c_shop_${System.currentTimeMillis()}"
        val shopName = "Chủ Cửa Hàng (Bạn)"
        val shopAvatarUrl = null // Default placeholder

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newComment = Comment(
            commentId = newCommentId,
            authorName = shopName,
            authorAvatarUrl = shopAvatarUrl,
            content = content,
            createdAt = timeStr,
            parentId = parentId
        )

        // Save to static map
        val existing = mockCommentsMap[postId] ?: mutableListOf()
        existing.add(newComment)
        mockCommentsMap[postId] = existing

        // Update current loaded list
        loadedComments.add(newComment)
        emitThreaded()

        _sendResult.value = null
    }

    private fun emitThreaded() {
        _uiState.value = CommentUiState.Success(buildThreadedList(loadedComments))
    }

    private fun buildThreadedList(flat: List<Comment>): List<Comment> {
        val parents = flat.filter { it.parentId == null }
        val repliesByParent = flat.filter { it.parentId != null }
            .groupBy { it.parentId!! }

        val result = mutableListOf<Comment>()
        for (parent in parents) {
            result.add(parent)
            val replies = repliesByParent[parent.commentId] ?: emptyList()
            // Replies under this parent: oldest first (chronological reading order)
            result.addAll(replies.reversed())
        }

        val knownParentIds = parents.map { it.commentId }.toSet()
        val orphans = flat.filter { it.parentId != null && it.parentId !in knownParentIds }
        result.addAll(orphans)

        return result
    }

    fun clearSendResult() {
        _sendResult.value = "CLEARED"
    }
}
