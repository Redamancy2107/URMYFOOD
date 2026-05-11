package com.urmyfood.user.presentation.main.home

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemFoodPostBinding
import com.urmyfood.user.domain.model.FoodPost
import java.text.NumberFormat
import java.util.Locale

class FoodPostAdapter : ListAdapter<FoodPost, FoodPostAdapter.ViewHolder>(DiffCallback()) {

    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFoodPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: FoodPost) {
            with(binding) {
                tvDishName.text = post.dishName
                tvContent.text = post.content ?: ""
                tvShopName.text = post.shopName
                tvPrice.text = "${currencyFormat.format(post.price)}đ"
                tvRemainingQuantity.text = "Còn ${post.remainingQuantity} suất"

                // Flash sale badge and original price
                if (post.isFlashSale && post.originalPrice > post.price) {
                    tvFlashSaleBadge.visibility = View.VISIBLE
                    tvOriginalPrice.visibility = View.VISIBLE
                    tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvOriginalPrice.text = "${currencyFormat.format(post.originalPrice)}đ"
                } else {
                    tvFlashSaleBadge.visibility = View.GONE
                    tvOriginalPrice.visibility = View.GONE
                }

                // Sold out overlay
                val isSoldOut = post.status == "SOLD_OUT" || post.remainingQuantity <= 0
                viewSoldOutOverlay.visibility = if (isSoldOut) View.VISIBLE else View.GONE
                tvSoldOutLabel.visibility = if (isSoldOut) View.VISIBLE else View.GONE

                // Dish image
                Glide.with(ivDishImage)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivDishImage)

                // Shop avatar
                Glide.with(ivShopAvatar)
                    .load(post.shopAvatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivShopAvatar)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<FoodPost>() {
        override fun areItemsTheSame(oldItem: FoodPost, newItem: FoodPost) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: FoodPost, newItem: FoodPost) =
            oldItem == newItem
    }
}
