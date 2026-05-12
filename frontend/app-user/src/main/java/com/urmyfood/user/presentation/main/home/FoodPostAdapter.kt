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
    
    var onCommentClick: (() -> Unit)? = null
    var onShareClick: (() -> Unit)? = null

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
            val ctx = binding.root.context
            with(binding) {
                // --- Header: shop info ---
                tvShopName.text = post.shopName
                
                // Mock metadata: Time + Dot + Location
                val mockTime = listOf("Vừa xong", "5 phút trước", "1 giờ trước", "3 giờ trước").random()
                val mockLocation = listOf("Hà Nội", "KTX Khu A", "Làng Đại học", "Thủ Đức").random()
                tvPostMeta.text = "$mockTime • $mockLocation"

                // --- Content description ---
                tvContent.text = post.content ?: post.dishName

                // --- Price ---
                tvPrice.text = "${currencyFormat.format(post.price)}đ"

                // --- Flash sale / Best seller badge ---
                if (post.isFlashSale) {
                    tvFlashSaleBadge.visibility = View.VISIBLE
                    tvFlashSaleBadge.text = ctx.getString(R.string.badge_flash_sale)
                    tvRemainingQuantity.visibility = View.VISIBLE
                    tvRemainingQuantity.text = "Còn lại: ${post.remainingQuantity} suất"
                    
                    tvOriginalPrice.visibility = View.VISIBLE
                    tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvOriginalPrice.text = "${currencyFormat.format(post.originalPrice)}đ"
                } else {
                    // Mock "Best Seller" for items with low remaining quantity or randomly
                    if (post.remainingQuantity < 10 || (0..1).random() == 1) {
                        tvFlashSaleBadge.visibility = View.VISIBLE
                        tvFlashSaleBadge.text = ctx.getString(R.string.badge_best_seller)
                    } else {
                        tvFlashSaleBadge.visibility = View.GONE
                    }
                    tvRemainingQuantity.visibility = View.GONE
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

                // --- Action buttons logic ---
                // Mock data
                tvLikeCount.text = "${(50..200).random()}"
                tvCommentCount.text = "${(2..30).random()}"

                // Follow toggle
                var isFollowing = false
                btnFollow.setOnClickListener {
                    isFollowing = !isFollowing
                    if (isFollowing) {
                        btnFollow.text = ctx.getString(R.string.following)
                        btnFollow.setTextColor(ctx.getColor(R.color.text_secondary))
                    } else {
                        btnFollow.text = ctx.getString(R.string.follow)
                        btnFollow.setTextColor(ctx.getColor(R.color.primary))
                    }
                }

                // Like toggle
                var isLiked = false
                btnLike.setOnClickListener {
                    isLiked = !isLiked
                    btnLike.setImageResource(if (isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
                }

                // Bookmark toggle
                var isBookmarked = false
                btnBookmark.setOnClickListener {
                    isBookmarked = !isBookmarked
                    btnBookmark.setImageResource(if (isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border)
                }

                btnComment.setOnClickListener {
                    onCommentClick?.invoke()
                }
                btnShare.setOnClickListener {
                    onShareClick?.invoke()
                }
                btnOrder.setOnClickListener {
                    Toast.makeText(ctx, R.string.toast_feature_in_development, Toast.LENGTH_SHORT).show()
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
