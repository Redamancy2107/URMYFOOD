package com.urmyfood.user.presentation.main.home

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
                // --- Header: shop info ---
                tvShopName.text = post.shopName
                tvPostMeta.text = post.endTime?.let { "Hết hạn lúc $it" } ?: "Đang mở bán"

                // --- Content description ---
                tvContent.text = post.content ?: post.dishName

                // --- Price ---
                tvPrice.text = "${currencyFormat.format(post.price)}đ"

                // --- Remaining quantity badge ---
                tvRemainingQuantity.text = "Còn lại: ${post.remainingQuantity} suất"

                // --- Flash sale badge and original price ---
                if (post.isFlashSale && post.originalPrice > post.price) {
                    tvFlashSaleBadge.visibility = View.VISIBLE
                    tvOriginalPrice.visibility = View.VISIBLE
                    tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvOriginalPrice.text = "${currencyFormat.format(post.originalPrice)}đ"
                } else {
                    tvFlashSaleBadge.visibility = View.GONE
                    tvOriginalPrice.visibility = View.GONE
                }

                // --- Sold out overlay ---
                val isSoldOut = post.status == "SOLD_OUT" || post.remainingQuantity <= 0
                viewSoldOutOverlay.visibility = if (isSoldOut) View.VISIBLE else View.GONE
                tvSoldOutLabel.visibility = if (isSoldOut) View.VISIBLE else View.GONE

                // --- Dish image ---
                Glide.with(ivDishImage)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.bg_food_banner)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivDishImage)

                // --- Shop avatar ---
                Glide.with(ivShopAvatar)
                    .load(post.shopAvatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivShopAvatar)

                // --- Action buttons (placeholder - feature in dev) ---
                // Mock data - remove when BE ready
                tvLikeCount.text = "${(50..200).random()}"
                tvCommentCount.text = "${(2..30).random()}"

                val ctx = root.context
                btnLike.setOnClickListener {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
                }
                btnComment.setOnClickListener {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
                }
                btnShare.setOnClickListener {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
                }
                btnBookmark.setOnClickListener {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
                }
                btnOrder.setOnClickListener {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
                }
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
