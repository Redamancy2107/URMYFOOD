package com.urmyfood.shop.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
            binding.tvAvatarInitial.text = session.customerName.firstOrNull()?.uppercase() ?: "?"
            binding.tvCustomerName.text = session.customerName
            binding.tvLastMessage.text = session.lastMessage ?: ""
            binding.tvTimestamp.text = session.lastMessageAt?.take(5) ?: ""

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
