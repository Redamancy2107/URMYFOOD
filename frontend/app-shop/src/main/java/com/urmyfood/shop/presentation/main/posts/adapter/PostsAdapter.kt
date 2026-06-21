package com.urmyfood.shop.presentation.main.posts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemPostCardBinding
import com.urmyfood.shop.domain.model.Post
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class PostsAdapter(
    private val listener: PostActionListener
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    interface PostActionListener {
        fun onSwitchToggle(postId: String, isActive: Boolean)
        fun onEditClick(post: Post)
        fun onItemClick(post: Post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

            binding.tvDishName.text = post.dishName
            binding.tvPrice.text = "${formatter.format(post.price)}₫"
            binding.tvStock.text = "Tồn kho: ${post.stock}"

            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = post.isActive
            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                listener.onSwitchToggle(post.postId, isChecked)
            }

            if (!post.imageUrl.isNullOrEmpty()) {
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

    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem
    }
}
