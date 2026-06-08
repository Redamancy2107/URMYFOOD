package com.urmyfood.shop.presentation.main.posts

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentPostDetailBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentPost: ShopPost

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        
        arguments?.getString("postJson")?.let { jsonStr ->
            try {
                val json = JSONObject(jsonStr)
                currentPost = ShopPost(
                    postId = json.getString("postId"),
                    dishName = json.getString("dishName"),
                    price = json.getLong("price"),
                    originalPrice = if (json.has("originalPrice")) json.getLong("originalPrice") else null,
                    content = if (json.has("content")) json.getString("content") else null,
                    imageUrl = json.getString("imageUrl"),
                    stock = json.getInt("stock"),
                    maxStock = json.getInt("maxStock"),
                    isActive = json.getBoolean("isActive"),
                    isFlashSale = json.getBoolean("isFlashSale"),
                    likeCount = json.getInt("likeCount"),
                    commentCount = json.getInt("commentCount"),
                    category = if (json.has("category")) json.getString("category") else null
                )
                bindPostData()
                setupControls()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu bài đăng", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Không tìm thấy dữ liệu bài đăng", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun bindPostData() {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

        // Bind shop metadata
        binding.tvShopName.text = "Quán Cửa Hàng Của Bạn"
        binding.tvPostMeta.text = "Vừa xong • KTX Khu A"
        binding.tvDescription.text = currentPost.content ?: ""
        binding.tvDescription.visibility = if (currentPost.content.isNullOrBlank()) View.GONE else View.VISIBLE

        // Bind pricing
        binding.tvPrice.text = "${formatter.format(currentPost.price)}₫"
        if (currentPost.originalPrice != null && currentPost.originalPrice!! > currentPost.price) {
            binding.tvOldPrice.text = "${formatter.format(currentPost.originalPrice)}₫"
            binding.tvOldPrice.paintFlags = binding.tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvOldPrice.visibility = View.VISIBLE
        } else {
            binding.tvOldPrice.visibility = View.GONE
        }

        // Bind Flash Sale badge
        binding.tagFlashSale.visibility = if (currentPost.isFlashSale) View.VISIBLE else View.GONE

        // Bind stock badge and controls
        updateStockUi()

        // Bind quick controls switch
        binding.switchQuickAvailable.isChecked = currentPost.isActive

        // Bind stats
        binding.tvLikeCount.text = currentPost.likeCount.toString()
        binding.tvCommentCount.text = currentPost.commentCount.toString()

        // Load image
        if (currentPost.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(currentPost.imageUrl)
                .placeholder(R.drawable.bg_card)
                .error(R.drawable.bg_card)
                .centerCrop()
                .into(binding.ivPostImage)
        } else {
            binding.ivPostImage.setImageResource(R.drawable.bg_card)
        }
    }

    private fun updateStockUi() {
        binding.tvBadge.text = "Còn lại: ${currentPost.stock} suất"
        binding.tvQuickStockCount.text = currentPost.stock.toString()
    }

    private fun setupControls() {
        // Quick Increment
        binding.btnQuickIncrement.setOnClickListener {
            val updatedPost = currentPost.copy(stock = currentPost.stock + 1)
            currentPost = updatedPost
            updateStockUi()
            Toast.makeText(requireContext(), "Đã tăng số suất ăn lên ${currentPost.stock}", Toast.LENGTH_SHORT).show()
        }

        // Quick Decrement
        binding.btnQuickDecrement.setOnClickListener {
            if (currentPost.stock > 0) {
                val updatedPost = currentPost.copy(stock = currentPost.stock - 1)
                currentPost = updatedPost
                updateStockUi()
                Toast.makeText(requireContext(), "Đã giảm số suất ăn xuống ${currentPost.stock}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Số suất ăn đã bằng 0", Toast.LENGTH_SHORT).show()
            }
        }

        // Quick Availability Switch
        binding.switchQuickAvailable.setOnCheckedChangeListener { _, isChecked ->
            val updatedPost = currentPost.copy(isActive = isChecked)
            currentPost = updatedPost
            val message = if (isChecked) "Đã mở nhận đơn cho món ăn" else "Đã tắt nhận đơn cho món ăn"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Edit Post Button
        binding.btnEditPost.setOnClickListener {
            try {
                val json = JSONObject().apply {
                    put("postId", currentPost.postId)
                    put("dishName", currentPost.dishName)
                    put("price", currentPost.price)
                    currentPost.originalPrice?.let { put("originalPrice", it) }
                    currentPost.content?.let { put("content", it) }
                    put("imageUrl", currentPost.imageUrl)
                    put("stock", currentPost.stock)
                    put("maxStock", currentPost.maxStock)
                    put("isActive", currentPost.isActive)
                    put("isFlashSale", currentPost.isFlashSale)
                    put("likeCount", currentPost.likeCount)
                    put("commentCount", currentPost.commentCount)
                    currentPost.category?.let { put("category", it) }
                }
                
                val bundle = Bundle().apply {
                    putString("postJson", json.toString())
                }
                findNavController().navigate(R.id.createPostFragment, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
