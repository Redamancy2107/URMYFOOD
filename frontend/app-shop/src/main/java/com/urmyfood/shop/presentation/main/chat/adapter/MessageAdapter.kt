package com.urmyfood.shop.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shop.databinding.ItemMessageReceivedBinding
import com.urmyfood.shop.databinding.ItemMessageSentBinding

class MessageAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (messages[position].senderRole == "SHOP") TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT)
            SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
        else
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SentViewHolder -> holder.bind(msg)
            is ReceivedViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount() = messages.size

    fun submitList(list: List<ChatMessage>) {
        messages.clear()
        messages.addAll(list)
        notifyDataSetChanged()
    }

    fun appendMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvSentMessage.text = msg.content
            // Backend returns ISO_LOCAL_DATE_TIME: "2025-06-20T14:30:00" — HH:mm starts at index 11
            binding.tvSentStatus.text = if (msg.sentAt.length >= 16) msg.sentAt.substring(11, 16) else msg.sentAt
        }
    }

    class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvReceivedMessage.text = msg.content
            binding.tvReceivedTime.text = if (msg.sentAt.length >= 16) msg.sentAt.substring(11, 16) else msg.sentAt
        }
    }
}
