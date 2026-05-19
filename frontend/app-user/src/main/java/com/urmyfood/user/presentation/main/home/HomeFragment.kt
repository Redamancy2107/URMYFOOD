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
import com.urmyfood.user.presentation.common.GuestLoginDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainHomeBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.util.BrandingHelper

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
        setupClickListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
        binding.rvPosts.isNestedScrollingEnabled = false

        // Load mock data immediately for FE preview (no backend needed)
        loadMockPosts()
    }

    private fun loadMockPosts() {
        val mockPosts = listOf(
            com.urmyfood.user.domain.model.FoodPost(
                postId = "1",
                dishName = "Trà sữa trân châu đường đen",
                price = 25000.0,
                originalPrice = 35000.0,
                maxQuantity = 100,
                remainingQuantity = 89,
                endTime = "14:00",
                isFlashSale = true,
                status = "ACTIVE",
                content = "Siêu béo, topping ngập tràn, free size L",
                imageUrl = "https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500",
                shopName = "Tiệm Trà Sữa Mây",
                shopAvatarUrl = null
            ),
            com.urmyfood.user.domain.model.FoodPost(
                postId = "2",
                dishName = "Cơm tấm sườn bì chả",
                price = 35000.0,
                originalPrice = 45000.0,
                maxQuantity = 50,
                remainingQuantity = 38,
                endTime = null,
                isFlashSale = false,
                status = "ACTIVE",
                content = "Cơm tấm đúng vị Sài Gòn, nước mắm kẹo đặc trưng",
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                shopName = "Cơm Tấm Bụi",
                shopAvatarUrl = null
            ),
            com.urmyfood.user.domain.model.FoodPost(
                postId = "3",
                dishName = "Bún bò Huế đặc biệt",
                price = 40000.0,
                originalPrice = 40000.0,
                maxQuantity = 30,
                remainingQuantity = 15,
                endTime = null,
                isFlashSale = false,
                status = "ACTIVE",
                content = "Nước lèo hầm xương 12 tiếng, giò heo, bò viên",
                imageUrl = "https://images.unsplash.com/photo-1625398407796-82650a8c135f?w=500",
                shopName = "Quán Bà Chiểu",
                shopAvatarUrl = null
            )
        )

        // Show posts immediately, skip shimmer
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.rvPosts.visibility = View.VISIBLE
        adapter.submitList(mockPosts)
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
                    // Fallback to mock data on error for demo purposes
                    if (adapter.currentList.isEmpty()) loadMockPosts()
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

        adapter.onCommentClick = { showCommentSheet() }
        adapter.onShareClick = { showShareSheet() }
        adapter.onOrderClick = { foodPost ->
            showGuestDialogOrRun {
                val orderSheet = OrderBottomSheetFragment(foodPost)
                orderSheet.show(childFragmentManager, OrderBottomSheetFragment.TAG)
            }
        }
        adapter.checkIsBookmarked = { post ->
            favoritesManager.isFavorite(post.postId)
        }
        adapter.onSaveClick = { post ->
            showGuestDialogOrRun {
                val isSaved = favoritesManager.toggleFavorite(post)
                val msg = if (isSaved) "Đã lưu bài viết" else "Đã bỏ lưu bài viết"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showShareSheet() {
        showGuestDialogOrRun {
            val shareSheet = ShareBottomSheetFragment()
            shareSheet.show(childFragmentManager, ShareBottomSheetFragment.TAG)
        }
    }

    private fun showCommentSheet() {
        showGuestDialogOrRun {
            val commentSheet = QuickCommentFragment()
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
            width = 500 // pixels
            height = ListPopupWindow.WRAP_CONTENT
            isModal = true
            setBackgroundDrawable(requireContext().getDrawable(R.drawable.bg_dropdown_menu))
            
            // Offset to look better
            verticalOffset = 8 
            
            setOnItemClickListener { _, _, position, _ ->
                val selected = items[position].first
                Toast.makeText(requireContext(), "Sắp xếp: $selected", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            show()
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
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
