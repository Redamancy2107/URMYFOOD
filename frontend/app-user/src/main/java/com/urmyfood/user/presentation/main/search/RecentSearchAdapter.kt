package com.urmyfood.user.presentation.main.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.user.databinding.ItemRecentSearchBinding

class RecentSearchAdapter : ListAdapter<String, RecentSearchAdapter.RecentSearchViewHolder>(DiffCallback) {

    var onQueryClick: ((String) -> Unit)? = null
    var onRemoveClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val binding = ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentSearchViewHolder(
        private val binding: ItemRecentSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) {
            binding.tvRecentQuery.text = query
            binding.root.setOnClickListener { onQueryClick?.invoke(query) }
            binding.btnRemoveRecent.setOnClickListener { onRemoveClick?.invoke(query) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem.equals(newItem, ignoreCase = true)
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
