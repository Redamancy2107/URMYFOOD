package com.urmyfood.shop.presentation.main.orders.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemDishDetailBinding
import java.text.NumberFormat
import java.util.Locale

class DishDetailAdapter : ListAdapter<DishDetailAdapter.DishItem, DishDetailAdapter.ViewHolder>(DiffCallback()) {

    data class DishItem(
        val name: String,
        val quantity: Int,
        val price: Long,
        val imageUrl: String? = null
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDishDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDishDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DishItem) {
            binding.tvDishName.text = item.name
            binding.tvQuantity.text = "${item.quantity}x"
            binding.tvDishPrice.text = formatCurrency(item.price)
            binding.ivDishImage.setImageResource(R.drawable.bg_food_banner)
        }

        private fun formatCurrency(amount: Long): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            return formatter.format(amount)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DishItem>() {
        override fun areItemsTheSame(oldItem: DishItem, newItem: DishItem): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: DishItem, newItem: DishItem): Boolean =
            oldItem == newItem
    }
}
