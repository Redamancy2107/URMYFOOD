package com.urmyfood.user.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.user.databinding.ItemChatListBinding
import com.urmyfood.user.presentation.model.ChatSession
import android.view.View

class ChatListAdapter(
    private val sessions: List<ChatSession>,
    private val onItemClick: (ChatSession) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val session = sessions[position]
        with(holder.binding) {
            tvChatName.text = session.name
            tvLastMessage.text = session.lastMessage
            tvChatTime.text = session.time
            ivChatAvatar.setImageResource(session.avatar)

            if (session.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = session.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }

            root.setOnClickListener { onItemClick(session) }
        }
    }

    override fun getItemCount() = sessions.size
}
