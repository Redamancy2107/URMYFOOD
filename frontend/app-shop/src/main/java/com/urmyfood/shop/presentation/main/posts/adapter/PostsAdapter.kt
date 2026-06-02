package com.urmyfood.shop.presentation.main.posts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemPostCardBinding
import com.urmyfood.shop.presentation.main.posts.ShopPost
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class PostsAdapter(
    private val listener: PostActionListener
) : ListAdapter<ShopPost, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    interface PostActionListener {
        fun onSwitchToggle(postId: String, isActive: Boolean)
        fun onEditClick(post: ShopPost)
        fun onItemClick(post: ShopPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: ShopPost) {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

            binding.tvDishName.text = post.dishName
            binding.tvPrice.text = "${formatter.format(post.price)}₫"
            binding.tvStock.text = "Tồn kho: ${post.stock}"

            // Set switch state without triggering listener
            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = post.isActive
            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                listener.onSwitchToggle(post.postId, isChecked)
            }

            // Load dish image with Glide
            if (post.imageUrl.isNotEmpty()) {
                Glide.with(binding.ivDishImage.context)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.bg_food_banner)
                    .centerCrop()
                    .into(binding.ivDishImage)
            } else {
                binding.ivDishImage.setImageResource(R.drawable.ic_image_placeholder)
            }

            binding.btnEdit.setOnClickListener { listener.onEditClick(post) }
            binding.root.setOnClickListener { listener.onItemClick(post) }
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<ShopPost>() {
        override fun areItemsTheSame(oldItem: ShopPost, newItem: ShopPost): Boolean =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: ShopPost, newItem: ShopPost): Boolean =
            oldItem == newItem
    }
}
