package com.urmyfood.user.presentation.main.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.user.R
import com.urmyfood.user.databinding.ItemPostBinding
import com.urmyfood.user.presentation.model.Post

class NewsfeedAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<NewsfeedAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvShopName.text = post.shopName
            tvPostMeta.text = post.time
            tvDescription.text = post.description
            tvPrice.text = post.price
            tvLikeCount.text = post.likeCount.toString()
            tvCommentCount.text = post.commentCount.toString()
            
            ivPostImage.setImageResource(post.image)
            ivShopAvatar.setImageResource(post.shopAvatar)

            // Flash Sale tag visibility
            tagFlashSale.visibility = if (post.isFlashSale) View.VISIBLE else View.GONE

            // Badge text
            tvBadge.text = post.badge ?: ""
            tvBadge.visibility = if (post.badge != null) View.VISIBLE else View.GONE

            // Old price visibility
            tvOldPrice.text = post.oldPrice
            tvOldPrice.visibility = if (post.oldPrice != null) View.VISIBLE else View.GONE

            // Like Interaction
            btnLike.setOnClickListener {
                post.isLiked = !post.isLiked
                if (post.isLiked) {
                    post.likeCount++
                    ivLikeIcon.setImageResource(R.drawable.ic_favorite) // Assuming filled heart
                } else {
                    post.likeCount--
                    ivLikeIcon.setImageResource(R.drawable.ic_favorite_border)
                }
                tvLikeCount.text = post.likeCount.toString()
            }

            // Comment Interaction
            btnComment.setOnClickListener {
                Toast.makeText(context, "Mở màn hình bình luận cho: ${post.shopName}", Toast.LENGTH_SHORT).show()
            }

            // Share Interaction
            btnShare.setOnClickListener {
                Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
            }

            // Bookmark/Save Interaction
            btnSave.setOnClickListener {
                Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
            }

            // Order button
            btnOrder.setOnClickListener {
                Toast.makeText(context, "Đã thêm ${post.shopName} vào giỏ hàng!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = posts.size
}
