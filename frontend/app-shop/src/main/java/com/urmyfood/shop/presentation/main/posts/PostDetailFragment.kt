package com.urmyfood.shop.presentation.main.posts

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentPostDetailBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.domain.model.Post
import java.text.NumberFormat
import java.util.Locale

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PostDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getString("postId") ?: run {
            Toast.makeText(requireContext(), "Không tìm thấy bài đăng", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        viewModel = ViewModelProvider(this, ServiceLocator.providePostDetailViewModelFactory(postId))
            .get(PostDetailViewModel::class.java)

        setupToolbar()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun observeData() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PostDetailUiState.Loading -> { /* progress */ }
                is PostDetailUiState.Success -> bindPost(state.post)
                is PostDetailUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            if (result is Result.Success) {
                Toast.makeText(requireContext(), "Đã xóa bài đăng thành công!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else if (result is Result.Error) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            }
            viewModel.clearDeleteResult()
        }
    }

    private fun bindPost(post: Post) {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

        binding.tvShopName.text = post.shopName
        binding.tvPostMeta.text = post.createdAt
        binding.tvDescription.text = post.content ?: ""
        binding.tvDescription.visibility = if (post.content.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.tvPrice.text = "${formatter.format(post.price)}₫"
        val orig = post.originalPrice
        if (orig != null && orig > post.price) {
            binding.tvOldPrice.text = "${formatter.format(orig)}₫"
            binding.tvOldPrice.paintFlags = binding.tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvOldPrice.visibility = View.VISIBLE
        } else {
            binding.tvOldPrice.visibility = View.GONE
        }

        binding.tagFlashSale.visibility = if (post.isFlashSale) View.VISIBLE else View.GONE
        binding.tvBadge.text = "Còn lại: ${post.stock} suất"
        binding.tvQuickStockCount.setText(post.stock.toString())

        binding.switchQuickAvailable.setOnCheckedChangeListener(null)
        binding.switchQuickAvailable.isChecked = post.isActive
        binding.switchQuickAvailable.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleStatus()
        }

        binding.tvLikeCount.text = post.likeCount.toString()
        binding.tvCommentCount.text = post.commentCount.toString()

        if (!post.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(post.imageUrl)
                .placeholder(R.drawable.bg_card)
                .error(R.drawable.bg_card)
                .centerCrop()
                .into(binding.ivPostImage)
        } else {
            binding.ivPostImage.setImageResource(R.drawable.bg_card)
        }

        binding.btnComment.setOnClickListener {
            val commentSheet = QuickCommentFragment.newInstance(post.postId)
            commentSheet.setOnCommentAddedListener {
                val newCount = post.commentCount + 1
                binding.tvCommentCount.text = newCount.toString()
            }
            commentSheet.show(childFragmentManager, QuickCommentFragment.TAG)
        }

        binding.btnDeletePost.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                .setPositiveButton("Xóa") { _, _ -> viewModel.deletePost() }
                .setNegativeButton("Hủy", null)
                .show()
        }

        binding.btnEditPost.setOnClickListener {
            val bundle = Bundle().apply { putString("postId", post.postId) }
            findNavController().navigate(R.id.action_postDetail_to_createPost, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
