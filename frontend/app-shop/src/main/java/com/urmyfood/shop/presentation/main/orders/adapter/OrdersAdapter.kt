package com.urmyfood.shop.presentation.main.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemOrderCardBinding
import com.urmyfood.shop.domain.model.Order
import java.text.NumberFormat
import java.util.Locale

class OrdersAdapter(
    private val onActionClick: (Order) -> Unit,
    private val onAcceptClick: (Order) -> Unit,
    private val onRejectClick: (Order) -> Unit,
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, OrdersAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemOrderCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderId.text = order.orderId.take(8).uppercase()
            binding.tvTimestamp.text = order.createdAt.take(16).replace("T", " ")
            binding.tvCustomerName.text = order.customerName
            binding.tvItemsSummary.text = order.items.joinToString(", ") { "${it.quantity}x ${it.dishNameSnapshot}" }
            binding.tvTotalPrice.text = formatCurrency(order.finalAmount.toLong())

            val (statusLabel, statusColorRes) = when (order.orderStatus) {
                "PENDING" -> "Chờ xác nhận" to R.color.warning
                "ACCEPTED" -> "Đã xác nhận" to R.color.primary
                "PICKING_UP" -> "Đang lấy hàng" to R.color.primary
                "DELIVERING" -> "Đang giao" to R.color.primary
                "COMPLETED" -> "Hoàn thành" to R.color.success
                "CANCELLED" -> "Đã hủy" to R.color.error
                "REJECTED" -> "Đã từ chối" to R.color.error
                "EXPIRED" -> "Hết hạn" to R.color.error
                else -> order.orderStatus to R.color.text_secondary
            }
            binding.tvStatusBadge.text = statusLabel
            binding.tvStatusBadge.setTextColor(binding.root.context.getColor(statusColorRes))

            when (order.orderStatus) {
                "PENDING" -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.text = "Xác nhận"
                    binding.btnAction.setOnClickListener { onAcceptClick(order) }
                    binding.btnReject.visibility = View.VISIBLE
                }
                "ACCEPTED", "PICKING_UP", "DELIVERING" -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.text = when (order.orderStatus) {
                        "ACCEPTED" -> "Lấy hàng"
                        "PICKING_UP" -> "Bắt đầu giao"
                        else -> "Hoàn thành"
                    }
                    binding.btnAction.setOnClickListener { onActionClick(order) }
                    binding.btnReject.visibility = View.GONE
                }
                else -> {
                    binding.btnAction.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE
                }
            }

            binding.btnReject.setOnClickListener { onRejectClick(order) }
            binding.root.setOnClickListener { onItemClick(order) }
        }

        private fun formatCurrency(amount: Long): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            return formatter.format(amount)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean =
            oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean =
            oldItem == newItem
    }
}
