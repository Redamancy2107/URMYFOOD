package com.urmyfood.shop.presentation.main.account.hours.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.shop.databinding.ItemBusinessDayBinding
import com.urmyfood.shop.presentation.main.account.hours.BusinessHoursViewModel.BusinessDay

class BusinessDaysAdapter(
    private val onDayToggled: (BusinessDay, Boolean) -> Unit
) : ListAdapter<BusinessDay, BusinessDaysAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBusinessDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemBusinessDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: BusinessDay) {
            binding.tvDayName.text = day.dayName
            binding.tvTimeRange.text = "${day.openTime} - ${day.closeTime}"

            // Setup visibility based on isOpen
            if (day.isOpen) {
                binding.tvTimeRange.visibility = View.VISIBLE
                binding.tvClosed.visibility = View.GONE
            } else {
                binding.tvTimeRange.visibility = View.GONE
                binding.tvClosed.visibility = View.VISIBLE
            }

            // Prevent infinite loop when updating switch programmatically
            binding.switchDay.setOnCheckedChangeListener(null)
            binding.switchDay.isChecked = day.isOpen
            binding.switchDay.setOnCheckedChangeListener { _, isChecked ->
                onDayToggled(day, isChecked)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<BusinessDay>() {
        override fun areItemsTheSame(oldItem: BusinessDay, newItem: BusinessDay): Boolean =
            oldItem.dayName == newItem.dayName

        override fun areContentsTheSame(oldItem: BusinessDay, newItem: BusinessDay): Boolean =
            oldItem == newItem
    }
}
