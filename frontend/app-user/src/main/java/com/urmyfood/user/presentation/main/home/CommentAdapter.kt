package com.urmyfood.user.presentation.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemCommentBinding
import com.urmyfood.user.domain.model.Comment
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class CommentAdapter : ListAdapter<Comment, CommentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvUserName.text = comment.authorName
            binding.tvComment.text = comment.content
            binding.tvTime.text = formatTime(comment.createdAt)
            Glide.with(binding.ivAvatar)
                .load(comment.authorAvatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem == newItem
    }

    private companion object {
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", Locale("vi"))

        fun formatTime(raw: String): String =
            try {
                OffsetDateTime.parse(raw).format(TIME_FORMATTER)
            } catch (e: DateTimeParseException) {
                raw
            }
    }
}
