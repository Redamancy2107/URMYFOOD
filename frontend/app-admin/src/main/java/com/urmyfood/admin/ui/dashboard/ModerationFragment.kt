package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.PostItem
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentModerationBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ModerationFragment : Fragment() {
    private var _binding: FragmentModerationBinding? = null
    private val binding get() = _binding!!
    private val repository = AdminRepository()
    private val postsList = mutableListOf<PostItem>()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModerationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(postsList, 
            onHide = { post -> togglePostStatus(post) },
            onDelete = { post -> confirmDeletePost(post) },
            onClick = { post -> showPostDetailDialog(post) }
        )
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        lifecycleScope.launch {
            val result = repository.getAllPosts(0, 50)
            result.onSuccess { page ->
                postsList.clear()
                postsList.addAll(page.content)
                adapter.notifyDataSetChanged()
                if (postsList.isEmpty()) {
                    Toast.makeText(requireContext(), "Không có bài đăng nào cần duyệt", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Lỗi tải bài viết: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPostDetailDialog(post: PostItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_post_detail, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val ivDishImage = dialogView.findViewById<ImageView>(R.id.iv_dish_image)
        val tvDishName = dialogView.findViewById<TextView>(R.id.tv_dialog_dish_name)
        val tvPrice = dialogView.findViewById<TextView>(R.id.tv_dialog_price)
        val tvOriginalPrice = dialogView.findViewById<TextView>(R.id.tv_dialog_original_price)
        val tvShopName = dialogView.findViewById<TextView>(R.id.tv_dialog_shop_name)
        val tvCategory = dialogView.findViewById<TextView>(R.id.tv_dialog_category)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tv_dialog_status)
        val tvContent = dialogView.findViewById<TextView>(R.id.tv_dialog_content)
        val btnClose = dialogView.findViewById<View>(R.id.btn_dialog_close)

        tvDishName.text = post.dishName ?: "Chưa xác định"
        
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvPrice.text = currencyFormat.format(post.price ?: 0.0)
        
        if (post.originalPrice != null && post.originalPrice > (post.price ?: 0.0)) {
            tvOriginalPrice.visibility = View.VISIBLE
            tvOriginalPrice.text = currencyFormat.format(post.originalPrice)
            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvOriginalPrice.visibility = View.GONE
        }

        tvShopName.text = "Cửa hàng: ${post.shopName ?: "Chưa rõ"}"
        tvCategory.text = "Danh mục: ${post.category ?: "Chưa rõ"}"
        
        val statusText = if (post.status == "HIDDEN") "Đã ẩn" else "Hiển thị"
        tvStatus.text = "Trạng thái: $statusText"
        tvContent.text = post.content ?: "Không có mô tả chi tiết."

        Glide.with(this)
            .load(post.imageUrl)
            .placeholder(R.drawable.ic_post_placeholder)
            .error(R.drawable.ic_post_placeholder)
            .into(ivDishImage)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.let { window ->
            // Adjust width for landscape mode on tablets (55% width is premium)
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.55).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun togglePostStatus(post: PostItem) {
        val newStatus = if (post.status == "HIDDEN") "APPROVED" else "HIDDEN"
        lifecycleScope.launch {
            val result = repository.moderatePostStatus(post.postId, newStatus)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã cập nhật trạng thái bài viết", Toast.LENGTH_SHORT).show()
                loadPosts()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Cập nhật thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeletePost(post: PostItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa bài viết")
            .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn bài đăng '${post.dishName}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                performDeletePost(post.postId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun performDeletePost(postId: String) {
        lifecycleScope.launch {
            val result = repository.deletePost(postId)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã xóa bài viết thành công", Toast.LENGTH_SHORT).show()
                loadPosts()
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Xóa thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter
    private class PostAdapter(
        private val items: List<PostItem>,
        private val onHide: (PostItem) -> Unit,
        private val onDelete: (PostItem) -> Unit,
        private val onClick: (PostItem) -> Unit
    ) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dishName: TextView = view.findViewById(R.id.tv_dish_name)
            val shopName: TextView = view.findViewById(R.id.tv_shop_name)
            val price: TextView = view.findViewById(R.id.tv_price)
            val status: TextView = view.findViewById(R.id.tv_status)
            val btnHide: ImageView = view.findViewById(R.id.btn_hide)
            val btnDelete: ImageView = view.findViewById(R.id.btn_delete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.dishName.text = item.dishName
            holder.shopName.text = item.shopName ?: "Chưa rõ"
            holder.price.text = currencyFormat.format(item.price ?: 0.0)
            
            val context = holder.itemView.context
            if (item.status == "HIDDEN") {
                holder.status.text = "Đã ẩn"
                holder.status.setTextColor(context.getColor(R.color.error_red))
                holder.btnHide.setImageResource(android.R.drawable.ic_menu_view)
            } else {
                holder.status.text = "Hiển thị"
                holder.status.setTextColor(context.getColor(R.color.mint_green))
                holder.btnHide.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            }

            holder.btnHide.setOnClickListener { onHide(item) }
            holder.btnDelete.setOnClickListener { onDelete(item) }
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
