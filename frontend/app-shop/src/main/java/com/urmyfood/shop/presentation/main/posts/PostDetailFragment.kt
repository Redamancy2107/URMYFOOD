package com.urmyfood.shop.presentation.main.posts

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.app.AlertDialog
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

    private val postsViewModel: PostsViewModel by viewModels()
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
                val initialPostId = json.getString("postId")
                val postInDb = PostsViewModel.allPosts.find { it.postId == initialPostId }
                currentPost = postInDb ?: ShopPost(
                    postId = initialPostId,
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
        val currentInput = binding.tvQuickStockCount.text.toString()
        val postStockStr = currentPost.stock.toString()
        if (currentInput != postStockStr) {
            binding.tvQuickStockCount.setText(postStockStr)
        }
    }

    private fun setupControls() {
        // Quick Increment
        binding.btnQuickIncrement.setOnClickListener {
            val updatedPost = currentPost.copy(stock = currentPost.stock + 1)
            currentPost = updatedPost
            updateStockUi()
            postsViewModel.updateStock(currentPost.postId, currentPost.stock)
            Toast.makeText(requireContext(), "Đã tăng số suất ăn lên ${currentPost.stock}", Toast.LENGTH_SHORT).show()
        }

        // Quick Decrement
        binding.btnQuickDecrement.setOnClickListener {
            if (currentPost.stock > 0) {
                val updatedPost = currentPost.copy(stock = currentPost.stock - 1)
                currentPost = updatedPost
                updateStockUi()
                postsViewModel.updateStock(currentPost.postId, currentPost.stock)
                Toast.makeText(requireContext(), "Đã giảm số suất ăn xuống ${currentPost.stock}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Số suất ăn đã bằng 0", Toast.LENGTH_SHORT).show()
            }
        }

        // Quick Availability Switch
        binding.switchQuickAvailable.setOnCheckedChangeListener { _, isChecked ->
            if (currentPost.isActive != isChecked) {
                val updatedPost = currentPost.copy(isActive = isChecked)
                currentPost = updatedPost
                postsViewModel.toggleActive(currentPost.postId)
                val message = if (isChecked) "Đã mở nhận đơn cho món ăn" else "Đã tắt nhận đơn cho món ăn"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // Comment Button Click
        binding.btnComment.setOnClickListener {
            val commentSheet = QuickCommentFragment.newInstance(currentPost.postId)
            commentSheet.setOnCommentAddedListener {
                val newCommentCount = CommentViewModel.mockCommentsMap[currentPost.postId]?.size ?: currentPost.commentCount
                val updatedPost = currentPost.copy(commentCount = newCommentCount)
                currentPost = updatedPost
                binding.tvCommentCount.text = newCommentCount.toString()
                
                // Update in local database too
                val index = PostsViewModel.allPosts.indexOfFirst { it.postId == currentPost.postId }
                if (index != -1) {
                    PostsViewModel.allPosts[index] = PostsViewModel.allPosts[index].copy(commentCount = newCommentCount)
                }
            }
            commentSheet.show(childFragmentManager, QuickCommentFragment.TAG)
        }

        // Delete Post Button Click
        binding.btnDeletePost.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                .setPositiveButton("Xóa") { _, _ ->
                    postsViewModel.deletePost(currentPost.postId)
                    Toast.makeText(requireContext(), "Đã xóa bài đăng thành công!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .setNegativeButton("Hủy", null)
                .show()
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

        // Text watcher for quick stock edit box
        binding.tvQuickStockCount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val input = s?.toString().orEmpty()
                val parsed = input.toIntOrNull()
                if (parsed != null && parsed != currentPost.stock) {
                    val updatedPost = currentPost.copy(stock = parsed)
                    currentPost = updatedPost
                    // Update layout badge without setting text to input box
                    binding.tvBadge.text = "Còn lại: ${currentPost.stock} suất"
                    postsViewModel.updateStock(currentPost.postId, currentPost.stock)
                }
            }
        })

        // Reset text if left empty on focus lost
        binding.tvQuickStockCount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = binding.tvQuickStockCount.text.toString()
                if (input.isEmpty() || input.toIntOrNull() == null) {
                    binding.tvQuickStockCount.setText(currentPost.stock.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
