package com.urmyfood.shop.presentation.main.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemOrderCardBinding
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel.OrderStatus
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel.ShopOrder
import java.text.NumberFormat
import java.util.Locale

class OrdersAdapter(
    private val onActionClick: (ShopOrder) -> Unit,
    private val onRejectClick: (ShopOrder) -> Unit,
    private val onItemClick: (ShopOrder) -> Unit
) : ListAdapter<ShopOrder, OrdersAdapter.ViewHolder>(DiffCallback()) {

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

        fun bind(order: ShopOrder) {
            binding.tvOrderId.text = order.orderId
            binding.tvTimestamp.text = order.timestamp
            binding.tvCustomerName.text = order.customerName
            binding.tvItemsSummary.text = order.items
            binding.tvTotalPrice.text = formatCurrency(order.totalPrice)

            // Status badge text
            binding.tvStatusBadge.text = order.status.label

            // Color-code status badge
            val badgeColorRes = when (order.status) {
                OrderStatus.WAITING -> R.color.warning
                OrderStatus.PREPARING -> R.color.primary
                OrderStatus.READY -> R.color.success
            }
            binding.tvStatusBadge.setTextColor(binding.root.context.getColor(badgeColorRes))

            // Action Button config
            when (order.status) {
                OrderStatus.WAITING -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.text = "Chuẩn bị"
                    binding.btnReject.visibility = View.VISIBLE
                }
                OrderStatus.PREPARING -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.text = "Sẵn sàng"
                    binding.btnReject.visibility = View.GONE
                }
                OrderStatus.READY -> {
                    binding.btnAction.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE
                }
            }

            binding.btnAction.setOnClickListener {
                onActionClick(order)
            }

            binding.btnReject.setOnClickListener {
                onRejectClick(order)
            }

            binding.root.setOnClickListener {
                onItemClick(order)
            }
        }

        private fun formatCurrency(amount: Long): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            return formatter.format(amount)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ShopOrder>() {
        override fun areItemsTheSame(oldItem: ShopOrder, newItem: ShopOrder): Boolean =
            oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(oldItem: ShopOrder, newItem: ShopOrder): Boolean =
            oldItem == newItem
    }
}
