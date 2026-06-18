package com.urmyfood.shop.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.databinding.ItemChatOrderCardBinding
import com.urmyfood.shop.databinding.ItemMessageReceivedBinding
import com.urmyfood.shop.databinding.ItemMessageSentBinding
import com.urmyfood.shop.presentation.model.Message

class MessageAdapter(
    private val messages: List<Message>,
    private val onOrderClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            else -> OrderViewHolder(ItemChatOrderCardBinding.inflate(inflater, parent, false), onOrderClick)
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

    class OrderViewHolder(
        val binding: ItemChatOrderCardBinding,
        private val onOrderClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val orderId = message.orderId ?: "#ORD-1001"
            binding.tvOrderTitle.text = "🍱 Đơn hàng $orderId"
            
            // Bind mock data fields based on orderId
            when (orderId) {
                "#ORD-1001" -> {
                    binding.tvOrderItemSummary.text = "2x Cơm tấm sườn, 1x Trà đá"
                    binding.tvOrderTotalPrice.text = "85.000đ"
                    binding.tvOrderStatus.text = "Đang chuẩn bị"
                }
                "#ORD-1002" -> {
                    binding.tvOrderItemSummary.text = "1x Bún bò Huế, 1x Sữa đậu nành"
                    binding.tvOrderTotalPrice.text = "55.000đ"
                    binding.tvOrderStatus.text = "Chờ xác nhận"
                }
                else -> {
                    binding.tvOrderItemSummary.text = "1x Cơm Tấm Sườn Bì Chả thêm trứng"
                    binding.tvOrderTotalPrice.text = "45.000đ"
                    binding.tvOrderStatus.text = "Đang chuẩn bị"
                }
            }
            
            binding.btnOrderDetail.setOnClickListener {
                onOrderClick(orderId)
            }
        }
    }
}
