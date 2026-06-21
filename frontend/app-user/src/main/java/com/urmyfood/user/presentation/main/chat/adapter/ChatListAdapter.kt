package com.urmyfood.user.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemChatListBinding

class ChatListAdapter(
    private val onItemClick: (ChatSession) -> Unit
) : ListAdapter<ChatSession, ChatListAdapter.ChatViewHolder>(ChatSessionDiffCallback()) {

    class ChatViewHolder(val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val session = getItem(position)
        with(holder.binding) {
            tvChatName.text = session.shopName
            tvLastMessage.text = session.lastMessage ?: ""
            tvChatTime.text = session.lastMessageAt?.take(5) ?: ""
            Glide.with(ivChatAvatar)
                .load(session.shopAvatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .centerCrop()
                .into(ivChatAvatar)

            if (session.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = session.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }

            root.setOnClickListener { onItemClick(session) }
        }
    }

    private class ChatSessionDiffCallback : DiffUtil.ItemCallback<ChatSession>() {
        override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean =
            oldItem == newItem
    }
}
