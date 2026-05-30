package com.urmyfood.shop.presentation.auth.registration.step3

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.ItemImageUploadCardBinding
import com.urmyfood.shop.databinding.ItemRegistrationPhotoBinding

class ShopPhotoAdapter(
    private val maxPhotos: Int,
    private val onRemoveClick: (index: Int) -> Unit,
    private val onAddClick: () -> Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_PHOTO = 0
        private const val TYPE_ADD = 1
    }

    override fun getItemCount(): Int {
        val photoCount = currentList.size
        return if (photoCount < maxPhotos) photoCount + 1 else photoCount
    }

    override fun getItemViewType(position: Int): Int =
        if (position < currentList.size) TYPE_PHOTO else TYPE_ADD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_PHOTO) {
            PhotoViewHolder(ItemRegistrationPhotoBinding.inflate(inflater, parent, false))
        } else {
            AddViewHolder(ItemImageUploadCardBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PhotoViewHolder -> holder.bind(currentList[position], position)
            is AddViewHolder -> holder.bind()
        }
    }

    inner class PhotoViewHolder(private val binding: ItemRegistrationPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: String, index: Int) {
            Glide.with(binding.root.context)
                .load(Uri.parse(uri))
                .centerCrop()
                .into(binding.ivPhoto)
            binding.btnRemove.setOnClickListener { onRemoveClick(index) }
        }
    }

    inner class AddViewHolder(private val binding: ItemImageUploadCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.ivPlaceholder.visibility = View.VISIBLE
            binding.ivPreview.visibility = View.GONE
            binding.root.setOnClickListener { onAddClick() }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
