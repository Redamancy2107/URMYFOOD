package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        ServiceLocator.provideHomeViewModelFactory()
    }

    private val adapter = FoodPostAdapter()

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
                imageUrl = null,
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
                imageUrl = null,
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
                imageUrl = null,
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
}
