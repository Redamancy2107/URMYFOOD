package com.urmyfood.user.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemMessageReceivedBinding
import com.urmyfood.user.databinding.ItemMessageReceivedImageBinding
import com.urmyfood.user.databinding.ItemMessageSentBinding
import com.urmyfood.user.databinding.ItemMessageSentImageBinding

class MessageAdapter(
    private val otherAvatarUrl: String? = null,
    private val messages: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
        private const val TYPE_SENT_IMAGE = 2
        private const val TYPE_RECEIVED_IMAGE = 3
    }

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        val isSent = msg.senderRole == "CUSTOMER"
        val isImage = msg.messageType == "IMAGE"
        return when {
            isSent && !isImage -> TYPE_SENT
            !isSent && !isImage -> TYPE_RECEIVED
            isSent && isImage -> TYPE_SENT_IMAGE
            else -> TYPE_RECEIVED_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
            TYPE_RECEIVED -> ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
            TYPE_SENT_IMAGE -> SentImageViewHolder(ItemMessageSentImageBinding.inflate(inflater, parent, false))
            else -> ReceivedImageViewHolder(ItemMessageReceivedImageBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SentViewHolder -> holder.bind(msg)
            is ReceivedViewHolder -> holder.bind(msg, otherAvatarUrl)
            is SentImageViewHolder -> holder.bind(msg)
            is ReceivedImageViewHolder -> holder.bind(msg, otherAvatarUrl)
        }
    }

    override fun getItemCount() = messages.size

    fun submitList(list: List<ChatMessage>) {
        val merged = linkedMapOf<Long, ChatMessage>()
        list.forEach { merged[it.id] = it }
        messages.forEach { existing -> merged.putIfAbsent(existing.id, existing) }

        messages.clear()
        messages.addAll(merged.values)
        notifyDataSetChanged()
    }

    fun appendMessage(message: ChatMessage) {
        val existingIndex = messages.indexOfFirst { it.id == message.id }
        if (existingIndex >= 0) {
            messages[existingIndex] = message
            notifyItemChanged(existingIndex)
            return
        }

        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvSentMessage.text = msg.content
            binding.tvSentStatus.text = formatTime(msg.sentAt)
        }
    }

    class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage, avatarUrl: String?) {
            binding.tvReceivedMessage.text = msg.content
            binding.tvReceivedTime.text = formatTime(msg.sentAt)
            Glide.with(itemView.context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivSenderAvatar)
        }
    }

    class SentImageViewHolder(private val binding: ItemMessageSentImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvSentTime.text = formatTime(msg.sentAt)
            Glide.with(itemView.context)
                .load(msg.imageUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(binding.ivSentImage)
        }
    }

    class ReceivedImageViewHolder(private val binding: ItemMessageReceivedImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage, avatarUrl: String?) {
            binding.tvReceivedTime.text = formatTime(msg.sentAt)
            Glide.with(itemView.context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivSenderAvatar)
            Glide.with(itemView.context)
                .load(msg.imageUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(binding.ivReceivedImage)
        }
    }

}

private fun formatTime(sentAt: String): String {
    if (sentAt.isEmpty()) return ""
    return if (sentAt.contains("T")) {
        val tIndex = sentAt.indexOf("T")
        if (sentAt.length >= tIndex + 6) {
            sentAt.substring(tIndex + 1, tIndex + 6)
        } else {
            sentAt
        }
    } else {
        if (sentAt.length >= 16) sentAt.substring(11, 16) else sentAt
    }
}
