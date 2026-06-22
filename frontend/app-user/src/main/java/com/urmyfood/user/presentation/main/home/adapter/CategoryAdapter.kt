package com.urmyfood.user.presentation.main.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.user.R
import com.urmyfood.user.presentation.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (String?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconBg: FrameLayout = view.findViewById(R.id.catIconBg)
        val tvIcon: TextView = view.findViewById(R.id.tvCatIcon)
        val tvName: TextView = view.findViewById(R.id.tvCatName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context
        val isSelected = position == selectedPosition

        holder.tvIcon.text = category.icon
        holder.tvName.text = category.name
        holder.iconBg.setBackgroundResource(
            if (isSelected) R.drawable.bg_category_active else R.drawable.bg_category_inactive
        )
        holder.tvName.setTextColor(
            ContextCompat.getColor(context, if (isSelected) R.color.primary else R.color.text_primary)
        )

        holder.itemView.setOnClickListener {
            val clicked = holder.bindingAdapterPosition
            if (clicked == RecyclerView.NO_POSITION || clicked == selectedPosition) return@setOnClickListener
            val previous = selectedPosition
            selectedPosition = clicked
            notifyItemChanged(previous)
            notifyItemChanged(clicked)
            onCategoryClick(categories[clicked].name.takeIf { it != ALL_CATEGORY })
        }
    }

    companion object {
        const val ALL_CATEGORY = "Tất cả"
    }

    override fun getItemCount() = categories.size
}
