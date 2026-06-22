package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.urmyfood.user.presentation.common.GuestLoginDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainHomeBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.util.BrandingHelper
import androidx.navigation.fragment.findNavController


/**
 * Home screen fragment.
 * Displays newsfeed, categories, and promotional content.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideHomeViewModelFactory()
    }

    private val adapter = FoodPostAdapter()
    private val favoritesManager = com.urmyfood.user.di.ServiceLocator.favoritesManager
    private var isFirstResume = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategories()
        setupSwipeRefresh()
        setupScrollListener()
        setupClickListeners()
        observeUiState()
        observeLoadingMore()
        observeLikeError()
        observeFollowError()
        observeSharedEvents()
        observeFilters()
    }

    private fun observeSharedEvents() {
        com.urmyfood.user.di.ServiceLocator.postCommentEvent.observe(viewLifecycleOwner) { postId ->
            viewModel.incrementCommentCount(postId)
            adapter.notifyDataSetChanged()
        }
        com.urmyfood.user.di.ServiceLocator.postLikeEvent.observe(viewLifecycleOwner) { event ->
            val (postId, likeData) = event
            val (isLiked, likeCount) = likeData
            viewModel.updateLikeStateExternally(postId, isLiked, likeCount)
            adapter.notifyDataSetChanged()
        }
        com.urmyfood.user.di.ServiceLocator.shopFollowEvent.observe(viewLifecycleOwner) { event ->
            val (shopId, isFollowing) = event
            viewModel.updateFollowStateForShop(shopId, isFollowing)
        }
    }

    private fun setupRecyclerView() {
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
        binding.rvPosts.isNestedScrollingEnabled = false
    }

    private fun setupCategories() {
        val categories = listOf(
            com.urmyfood.user.presentation.model.Category(1, "Cơm", "🍜"),
            com.urmyfood.user.presentation.model.Category(2, "Bún/Phở", "🥣"),
            com.urmyfood.user.presentation.model.Category(3, "Trà sữa", "🧋"),
            com.urmyfood.user.presentation.model.Category(4, "Ăn vặt", "🍢"),
            com.urmyfood.user.presentation.model.Category(5, "Bánh mì", "🥖")
        )
        binding.rvCategories.adapter =
            com.urmyfood.user.presentation.main.home.adapter.CategoryAdapter(categories)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }
    }

    private fun setupScrollListener() {
        val nestedScroll = binding.root.parent?.let {
            generateSequence(binding.root as View) { it.parent as? View }
                .filterIsInstance<NestedScrollView>()
                .firstOrNull()
        }
        (binding.root as? NestedScrollView
            ?: binding.swipeRefresh.getChildAt(0) as? NestedScrollView)
            ?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                val threshold = 200
                if (scrollY >= v.getChildAt(0).measuredHeight - v.measuredHeight - threshold) {
                    viewModel.loadMore()
                }
            })
    }

    private fun observeLoadingMore() {
        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
            binding.pbLoadingMore.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun observeLikeError() {
        viewModel.likeError.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            viewModel.clearLikeError()
        }
    }

    private fun observeFollowError() {
        viewModel.followError.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFollowError()
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NewsfeedUiState.Loading -> {
                    if (adapter.currentList.isEmpty()) {
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.startShimmer()
                        binding.rvPosts.visibility = View.GONE
                    }
                    binding.tvError.visibility = View.GONE
                }
                is NewsfeedUiState.Success -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    if (state.posts.isNotEmpty()) {
                        binding.tvError.visibility = View.GONE
                        binding.rvPosts.visibility = View.VISIBLE
                        adapter.submitList(state.posts)
                    }
                }
                is NewsfeedUiState.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE

                    if (adapter.currentList.isEmpty()) {
                        binding.rvPosts.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener { showFeatureInDevelopment() }
        binding.tvSeeAll.setOnClickListener { showFeatureInDevelopment() }
        
        binding.btnFilterPrice.setOnClickListener { view ->
            showPriceFilterMenu(view)
        }

        binding.btnFilterDate.setOnClickListener { view ->
            showDateFilterMenu(view)
        }

        binding.btnFilterFlash.setOnClickListener {
            viewModel.toggleFlashSaleFilter()
        }

        adapter.onCommentClick = { postId -> showCommentSheet(postId) }
        adapter.onShareClick = { showShareSheet() }
        adapter.onOrderClick = { foodPost ->
            showGuestDialogOrRun {
                val orderSheet = OrderBottomSheetFragment(foodPost)
                orderSheet.show(childFragmentManager, OrderBottomSheetFragment.TAG)
            }
        }
        adapter.onLikeClick = { post ->
            showGuestDialogOrRun {
                viewModel.toggleLike(post.postId, post.isLiked)
            }
        }
        adapter.onFollowClick = { post ->
            showGuestDialogOrRun {
                viewModel.toggleFollow(post.shopAccountId, post.isFollowingShop)
            }
        }
        adapter.checkIsBookmarked = { post ->
            if (viewModel.isGuest) {
                false
            } else {
                favoritesManager.isFavorite(post.postId)
            }
        }
        adapter.onSaveClick = { post ->
            showGuestDialogOrRun {
                val isSaved = favoritesManager.toggleFavorite(post)
                val msg = if (isSaved) "Đã lưu bài viết" else "Đã bỏ lưu bài viết"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
        }
        adapter.onShopClick = { post ->
            val bundle = Bundle().apply {
                putString("shopName", post.shopName)
                putString("shopAvatarUrl", post.shopAvatarUrl)
                putLong("shopId", post.shopAccountId)
            }
            findNavController().navigate(R.id.shopProfileFragment, bundle)
        }
    }

    private fun showShareSheet() {
        showGuestDialogOrRun {
            val shareSheet = ShareBottomSheetFragment()
            shareSheet.show(childFragmentManager, ShareBottomSheetFragment.TAG)
        }
    }

    private fun showCommentSheet(postId: String) {
        showGuestDialogOrRun {
            val commentSheet = QuickCommentFragment.newInstance(postId)
            commentSheet.show(childFragmentManager, QuickCommentFragment.TAG)
        }
    }

    private fun showGuestDialogOrRun(action: () -> Unit) {
        if (viewModel.isGuest) {
            showGuestLoginDialog()
        } else {
            action()
        }
    }

    private fun showGuestLoginDialog() {
        val dialog = GuestLoginDialog()
        dialog.onLoginClick = {
            com.urmyfood.user.di.ServiceLocator.tokenManager.clear()
            com.urmyfood.user.di.ServiceLocator.guestSessionManager.clearGuest()
            navigateToLogin()
        }
        dialog.onRegisterClick = {
            com.urmyfood.user.di.ServiceLocator.tokenManager.clear()
            com.urmyfood.user.di.ServiceLocator.guestSessionManager.clearGuest()
            navigateToRegister()
        }
        dialog.show(parentFragmentManager, GuestLoginDialog.TAG)
    }

    private fun navigateToLogin() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.let {
            it.navigate(R.id.loginFragment)
        }
    }

    private fun navigateToRegister() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.let {
            it.navigate(R.id.signupCustomerFragment)
        }
    }

    private fun showPriceFilterMenu(anchor: View) {
        val items = listOf(
            Pair("Từ thấp -> cao", R.drawable.ic_trending_up),
            Pair("Từ cao -> thấp", R.drawable.ic_trending_down)
        )

        val adapter = object : ArrayAdapter<Pair<String, Int>>(
            requireContext(),
            R.layout.item_dropdown_menu,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown_menu, parent, false)
                
                val item = getItem(position)
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
                
                tvTitle.text = item?.first
                item?.second?.let { ivIcon.setImageResource(it) }
                
                return view
            }
        }

        ListPopupWindow(requireContext()).apply {
            setAdapter(adapter)
            setAnchorView(anchor)
            width = 500 
            height = ListPopupWindow.WRAP_CONTENT
            isModal = true
            setBackgroundDrawable(requireContext().getDrawable(R.drawable.bg_dropdown_menu))
            
            verticalOffset = 8 
            
            setOnItemClickListener { _, _, position, _ ->
                val selected = items[position].first
                val order = if (position == 0) "LOW_TO_HIGH" else "HIGH_TO_LOW"
                viewModel.setSortOrder(order)
                Toast.makeText(requireContext(), "Sắp xếp: $selected", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            show()
        }
    }

    private fun showDateFilterMenu(anchor: View) {
        val items = listOf(
            Pair("Mới nhất", R.drawable.ic_trending_up),
            Pair("Cũ nhất", R.drawable.ic_trending_down)
        )

        val adapter = object : ArrayAdapter<Pair<String, Int>>(
            requireContext(),
            R.layout.item_dropdown_menu,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown_menu, parent, false)
                
                val item = getItem(position)
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
                
                tvTitle.text = item?.first
                item?.second?.let { ivIcon.setImageResource(it) }
                
                return view
            }
        }

        ListPopupWindow(requireContext()).apply {
            setAdapter(adapter)
            setAnchorView(anchor)
            width = 500 
            height = ListPopupWindow.WRAP_CONTENT
            isModal = true
            setBackgroundDrawable(requireContext().getDrawable(R.drawable.bg_dropdown_menu))
            verticalOffset = 8 
            
            setOnItemClickListener { _, _, position, _ ->
                val selected = items[position].first
                val order = if (position == 0) "NEWEST" else "OLDEST"
                viewModel.setSortOrder(order)
                Toast.makeText(requireContext(), "Sắp xếp: $selected", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            show()
        }
    }

    private fun observeFilters() {
        viewModel.isFlashSaleFilterActive.observe(viewLifecycleOwner) { isActive ->
            val ctx = requireContext()
            if (isActive) {
                binding.btnFilterFlash.setBackgroundColor(ctx.getColor(R.color.primary))
                binding.btnFilterFlash.setTextColor(ctx.getColor(android.R.color.white))
            } else {
                binding.btnFilterFlash.setBackgroundColor(ctx.getColor(R.color.white))
                binding.btnFilterFlash.setTextColor(ctx.getColor(R.color.primary))
            }
        }
    }

    private fun showFeatureInDevelopment() {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            adapter.notifyDataSetChanged()
        } else {
            viewModel.loadPosts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
