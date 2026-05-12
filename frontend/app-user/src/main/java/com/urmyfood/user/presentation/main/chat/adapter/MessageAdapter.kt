package com.urmyfood.user.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.user.databinding.ItemChatOrderCardBinding
import com.urmyfood.user.databinding.ItemMessageReceivedBinding
import com.urmyfood.user.databinding.ItemMessageSentBinding
import com.urmyfood.user.presentation.model.Message

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
        private const val TYPE_ORDER = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isOrderCard -> TYPE_ORDER
            message.isSent -> TYPE_SENT
            else -> TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
            TYPE_RECEIVED -> ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
            else -> OrderViewHolder(ItemChatOrderCardBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is ReceivedViewHolder -> holder.bind(message)
            is OrderViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvSentMessage.text = message.content
            binding.tvSentStatus.text = message.time
        }
    }

    class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvReceivedMessage.text = message.content
            binding.tvReceivedTime.text = message.time
        }
    }

    class OrderViewHolder(val binding: ItemChatOrderCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            // Order card uses hardcoded data from the layout for now
        }
    }
}
