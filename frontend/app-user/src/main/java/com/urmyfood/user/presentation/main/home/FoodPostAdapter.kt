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
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class FoodPostAdapter : ListAdapter<FoodPost, FoodPostAdapter.ViewHolder>(DiffCallback()) {

    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    var onCommentClick: ((String) -> Unit)? = null
    var onShareClick: (() -> Unit)? = null
    var onOrderClick: ((FoodPost) -> Unit)? = null
    var onSaveClick: ((FoodPost) -> Unit)? = null
    var onLikeClick: ((FoodPost) -> Unit)? = null
    var onFollowClick: ((FoodPost) -> Unit)? = null
    var checkIsBookmarked: ((FoodPost) -> Boolean)? = null
    var onShopClick: ((FoodPost) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Chuyển timestamp ISO (UTC) thành chuỗi thời gian tương đối; null nếu không parse được. */
    private fun relativeTime(iso: String?): String? {
        if (iso.isNullOrBlank()) return null
        return runCatching {
            val minutes = Duration.between(OffsetDateTime.parse(iso).toInstant(), Instant.now()).toMinutes()
            when {
                minutes < 1 -> "Vừa xong"
                minutes < 60 -> "$minutes phút trước"
                minutes < 60 * 24 -> "${minutes / 60} giờ trước"
                minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)} ngày trước"
                else -> OffsetDateTime.parse(iso)
                    .atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")))
            }
        }.getOrNull()
    }

    inner class ViewHolder(private val binding: ItemFoodPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: FoodPost) {
            val ctx = binding.root.context
            with(binding) {
                // --- Header: shop info ---
                tvShopName.text = post.shopName
                
                // Thời gian đăng (tương đối) + địa chỉ shop thật; bỏ qua phần nào thiếu dữ liệu
                val timeText = relativeTime(post.createdAt)
                val locationText = post.shopAddress?.takeIf { it.isNotBlank() }
                tvPostMeta.text = listOfNotNull(timeText, locationText).joinToString(" • ")

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
                    if (post.remainingQuantity in 1..9) {
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
                tvCommentCount.text = "${post.commentCount}"
                tvLikeCount.text = "${post.likeCount}"
                btnLike.setImageResource(if (post.isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)

                if (post.isFollowingShop) {
                    btnFollow.text = ctx.getString(R.string.following)
                    btnFollow.setTextColor(ctx.getColor(R.color.text_secondary))
                } else {
                    btnFollow.text = ctx.getString(R.string.follow)
                    btnFollow.setTextColor(ctx.getColor(R.color.primary))
                }
                btnFollow.setOnClickListener {
                    onFollowClick?.invoke(post)
                }

                // Like toggle
                btnLike.setOnClickListener {
                    onLikeClick?.invoke(post)
                }

                // Bookmark toggle
                val isBookmarked = checkIsBookmarked?.invoke(post) ?: false
                btnBookmark.setImageResource(if (isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border)
                btnBookmark.setOnClickListener {
                    onSaveClick?.invoke(post)
                }

                btnComment.setOnClickListener {
                    onCommentClick?.invoke(post.postId)
                }
                btnShare.setOnClickListener {
                    onShareClick?.invoke()
                }
                btnOrder.setOnClickListener {
                    onOrderClick?.invoke(post)
                }
                ivShopAvatar.setOnClickListener {
                    onShopClick?.invoke(post)
                }
                tvShopName.setOnClickListener {
                    onShopClick?.invoke(post)
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
