package com.urmyfood.shop.presentation.main.home.adapter

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemActiveOrderBinding
import com.urmyfood.shop.presentation.main.home.HomeViewModel.ActiveOrder
import com.urmyfood.shop.presentation.main.home.HomeViewModel.OrderStatus
import java.text.NumberFormat
import java.util.Locale

class ActiveOrdersAdapter(
    private val onActionClick: (ActiveOrder) -> Unit,
    private val onOrderClick: (ActiveOrder) -> Unit
) : ListAdapter<ActiveOrder, ActiveOrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemActiveOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemActiveOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: ActiveOrder) {
            binding.tvOrderId.text = "#${order.orderId.take(8).uppercase()}"
            binding.tvOrderTime.text = order.time
            binding.tvCustomerName.text = order.customerName
            binding.tvStatusBadge.text = order.status.label
            binding.tvItemsSummary.text = order.itemsSummary
            binding.tvTotalPrice.text = formatCurrency(order.totalPrice)

            com.bumptech.glide.Glide.with(binding.root.context)
                .load(order.imageUrl ?: R.drawable.bg_food_banner)
                .centerCrop()
                .into(binding.ivOrderImage)

            // Set status badge color
            val badgeColor = when (order.status) {
                OrderStatus.WAITING -> R.color.warning
                OrderStatus.PREPARING -> R.color.primary
            }
            binding.tvStatusBadge.setTextColor(
                binding.root.context.getColor(badgeColor)
            )

            // Action Button configuration
            when (order.status) {
                OrderStatus.WAITING -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.text = "Chuẩn bị"
                }
                OrderStatus.PREPARING -> {
                    binding.btnAction.visibility = View.VISIBLE
                    binding.btnAction.text = "Sẵn sàng"
                }
            }

            binding.btnAction.setOnClickListener {
                onActionClick(order)
            }

            binding.root.setOnClickListener { onOrderClick(order) }
        }

        private fun formatCurrency(amount: Long): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            return formatter.format(amount)
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<ActiveOrder>() {
        override fun areItemsTheSame(oldItem: ActiveOrder, newItem: ActiveOrder): Boolean =
            oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(oldItem: ActiveOrder, newItem: ActiveOrder): Boolean =
            oldItem == newItem
    }
}
