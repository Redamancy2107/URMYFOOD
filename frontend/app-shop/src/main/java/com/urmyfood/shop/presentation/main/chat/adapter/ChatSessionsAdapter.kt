package com.urmyfood.shop.presentation.main.chat.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.shop.databinding.ItemChatSessionBinding

class ChatSessionsAdapter(
    private val onSessionClick: (ChatSession) -> Unit
) : ListAdapter<ChatSession, ChatSessionsAdapter.ChatSessionViewHolder>(ChatSessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSessionViewHolder {
        val binding = ItemChatSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatSessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatSessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatSessionViewHolder(
        private val binding: ItemChatSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ChatSession) {
            binding.tvCustomerName.text = session.customerName
            binding.tvLastMessage.text = session.lastMessage ?: ""
            val timeStr = session.lastMessageAt?.let { at ->
                if (at.length >= 16 && at.contains("T")) {
                    val tIndex = at.indexOf("T")
                    at.substring(tIndex + 1, tIndex + 6)
                } else {
                    at.take(5)
                }
            } ?: ""
            binding.tvTimestamp.text = timeStr

            // Bold unread messages
            if (session.unreadCount > 0) {
                binding.tvLastMessage.setTypeface(null, Typeface.BOLD)
                binding.tvLastMessage.setTextColor(itemView.context.getColor(com.urmyfood.shop.R.color.text_primary))
            } else {
                binding.tvLastMessage.setTypeface(null, Typeface.NORMAL)
                binding.tvLastMessage.setTextColor(itemView.context.getColor(com.urmyfood.shop.R.color.text_secondary))
            }

            // Customer avatar (đồng nhất với app-user: placeholder/error, không dùng chữ-cái)
            Glide.with(itemView.context)
                .load(session.customerAvatarUrl)
                .placeholder(com.urmyfood.shop.R.drawable.ic_person_placeholder)
                .error(com.urmyfood.shop.R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivCustomerAvatar)

            if (session.unreadCount > 0) {
                binding.tvUnreadBadge.visibility = View.VISIBLE
                binding.tvUnreadBadge.text = session.unreadCount.toString()
            } else {
                binding.tvUnreadBadge.visibility = View.GONE
            }

            binding.root.setOnClickListener { onSessionClick(session) }
        }
    }

    private class ChatSessionDiffCallback : DiffUtil.ItemCallback<ChatSession>() {
        override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean =
            oldItem == newItem
    }
}
