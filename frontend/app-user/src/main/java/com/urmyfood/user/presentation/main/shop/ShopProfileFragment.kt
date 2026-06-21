package com.urmyfood.user.presentation.main.shop

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentShopProfileBinding
import com.urmyfood.user.databinding.ItemShopCategoryBinding
import com.urmyfood.user.databinding.ItemShopProductBinding
import com.urmyfood.user.databinding.ItemShopVoucherBinding
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.presentation.main.home.FoodPostAdapter
import com.urmyfood.user.presentation.main.home.OrderBottomSheetFragment
import com.urmyfood.user.presentation.main.home.QuickCommentFragment
import com.urmyfood.user.presentation.main.home.ShareBottomSheetFragment
import java.text.NumberFormat
import java.util.Locale

class ShopProfileFragment : Fragment() {

    private var _binding: FragmentShopProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShopProfileViewModel by viewModels()

    private lateinit var shopNameArg: String
    private var shopAvatarUrlArg: String? = null

    // Adapters for the lists
    private val shopPostsAdapter = FoodPostAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shopNameArg = it.getString("shopName") ?: "Cửa hàng"
            shopAvatarUrlArg = it.getString("shopAvatarUrl")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbarScrollAnimation()
        setupClickListeners()
        observeViewModel()

        viewModel.initShop(shopNameArg, shopAvatarUrlArg)
    }

    private fun setupToolbarScrollAnimation() {
        binding.appBarLayout.addOnOffsetChangedListener { appBar, verticalOffset ->
            val scrollRange = appBar.totalScrollRange
            if (scrollRange > 0) {
                val percentage = Math.abs(verticalOffset).toFloat() / scrollRange.toFloat()
                
                // Transition toolbar state
                if (percentage >= 0.7f) {
                    binding.tvToolbarTitle.visibility = View.VISIBLE
                    binding.tvToolbarTitle.alpha = (percentage - 0.7f) / 0.3f
                } else {
                    binding.tvToolbarTitle.visibility = View.INVISIBLE
                }
            }
        }
    }


    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSearch.setOnClickListener {
            showFeatureInDevelopment()
        }

        binding.btnShare.setOnClickListener {
            val shareSheet = ShareBottomSheetFragment()
            shareSheet.show(childFragmentManager, ShareBottomSheetFragment.TAG)
        }



        binding.btnFollow.setOnClickListener {
            viewModel.toggleFollow()
        }

        binding.btnChat.setOnClickListener {
            // Navigate directly to chat detail
            findNavController().navigate(R.id.chatDetailFragment)
        }

        // Setup callbacks for the feed list (Home Tab)
        shopPostsAdapter.onCommentClick = {
            val commentSheet = QuickCommentFragment()
            commentSheet.show(childFragmentManager, QuickCommentFragment.TAG)
        }
        shopPostsAdapter.onShareClick = {
            val shareSheet = ShareBottomSheetFragment()
            shareSheet.show(childFragmentManager, ShareBottomSheetFragment.TAG)
        }
        shopPostsAdapter.onOrderClick = { foodPost ->
            val orderSheet = OrderBottomSheetFragment(foodPost)
            orderSheet.show(childFragmentManager, OrderBottomSheetFragment.TAG)
        }
        shopPostsAdapter.onLikeClick = { post ->
            viewModel.toggleLike(post.postId)
        }
        shopPostsAdapter.onSaveClick = { post ->
            Toast.makeText(requireContext(), "Đã lưu bài viết của shop", Toast.LENGTH_SHORT).show()
        }
    }



    private fun observeViewModel() {
        viewModel.shopName.observe(viewLifecycleOwner) { name ->
            binding.tvShopName.text = name
            binding.tvToolbarTitle.text = name
        }

        viewModel.shopAvatarUrl.observe(viewLifecycleOwner) { avatarUrl ->
            Glide.with(this@ShopProfileFragment)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivShopAvatar)
        }

        viewModel.followers.observe(viewLifecycleOwner) { followersText ->
            binding.tvFollowers.text = followersText
        }

        viewModel.isFollowing.observe(viewLifecycleOwner) { isFollowing ->
            if (isFollowing) {
                binding.btnFollow.text = getString(R.string.following)
                binding.btnFollow.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                binding.btnFollow.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.input_bg)
                )
            } else {
                binding.btnFollow.text = getString(R.string.follow)
                binding.btnFollow.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.btnFollow.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
        }



        viewModel.address.observe(viewLifecycleOwner) { address ->
            binding.tvShopAddress.text = address
        }

        viewModel.operatingHours.observe(viewLifecycleOwner) { hours ->
            binding.tvShopHours.text = hours
        }

        viewModel.shopCategory.observe(viewLifecycleOwner) { category ->
            binding.tvShopCategory.text = "Món chủ đạo: $category"
        }

        viewModel.isOpen.observe(viewLifecycleOwner) { isOpen ->
            if (isOpen) {
                binding.tvShopStatus.text = "Đang mở cửa"
                binding.tvShopStatus.setTextColor(android.graphics.Color.parseColor("#10B981"))
            } else {
                binding.tvShopStatus.text = "Đang đóng cửa"
                binding.tvShopStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"))
            }
        }

        // Render horizontal vouchers
        viewModel.vouchers.observe(viewLifecycleOwner) { vouchersList ->
            binding.rvVouchers.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = VoucherAdapter(vouchersList)
            }
        }

        // Render Home tab posts
        viewModel.posts.observe(viewLifecycleOwner) { postsList ->
            binding.rvShopPosts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = shopPostsAdapter
            }
            shopPostsAdapter.submitList(postsList)
        }
    }

    private fun showFeatureInDevelopment() {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Inner adapters ---

    private class VoucherAdapter(private val vouchers: List<ShopVoucher>) :
        RecyclerView.Adapter<VoucherAdapter.ViewHolder>() {

        class ViewHolder(private val binding: ItemShopVoucherBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(voucher: ShopVoucher) {
                binding.tvVoucherTitle.text = voucher.title
                binding.tvVoucherDesc.text = voucher.description
                binding.tvVoucherExpiry.text = voucher.expiryDate
                
                binding.btnSaveVoucher.setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "Đã lưu Voucher: ${voucher.title} thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSaveVoucher.text = "Đã lưu"
                    binding.btnSaveVoucher.isEnabled = false
                    binding.btnSaveVoucher.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(itemView.context, R.color.input_bg)
                    )
                    binding.btnSaveVoucher.setTextColor(
                        androidx.core.content.ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                }

                binding.btnVoucherTerms.setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "Điều kiện: Áp dụng cho mọi đơn hàng của shop từ 50K.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemShopVoucherBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(vouchers[position])
        }

        override fun getItemCount() = vouchers.size
    }


}
