package com.urmyfood.user.presentation.main.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemCartBinding
import com.urmyfood.user.domain.model.CartItem
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter dùng để hiển thị danh sách các món ăn trong Giỏ hàng.
 * Kế thừa ListAdapter + DiffUtil để cập nhật danh sách tối ưu.
 */
class CartAdapter : ListAdapter<CartItem, CartAdapter.ViewHolder>(DiffCallback()) {

    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    var onQuantityChanged: ((CartItem, Int) -> Unit)? = null
    var onDeleteClicked: ((CartItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val ctx = binding.root.context
            with(binding) {
                // Bind Text
                tvCartShopName.text = item.shopName
                tvCartDishName.text = item.dishName
                
                if (item.selectedOption.isNullOrEmpty()) {
                    tvCartOption.visibility = ViewGroup.GONE
                } else {
                    tvCartOption.visibility = ViewGroup.VISIBLE
                    tvCartOption.text = item.selectedOption
                }
                
                tvCartPrice.text = "${currencyFormat.format(item.price)}đ"
                tvCartQuantity.text = item.quantity.toString()

                // Bind Image
                Glide.with(ivCartDishImage)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.bg_food_banner)
                    .into(ivCartDishImage)

                // Bind Controls
                btnCartPlus.setOnClickListener {
                    onQuantityChanged?.invoke(item, item.quantity + 1)
                }

                btnCartMinus.setOnClickListener {
                    onQuantityChanged?.invoke(item, item.quantity - 1)
                }

                btnCartDelete.setOnClickListener {
                    onDeleteClicked?.invoke(item)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) =
            oldItem.postId == newItem.postId && oldItem.selectedOption == newItem.selectedOption

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) =
            oldItem == newItem
    }
}
