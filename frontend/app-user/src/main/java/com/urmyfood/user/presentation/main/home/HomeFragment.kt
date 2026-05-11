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
        BrandingHelper.styleAppName(binding.tvLogo)
        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
        binding.rvPosts.isNestedScrollingEnabled = false
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
                    // Only show shimmer on initial load (no data yet).
                    // During swipe-to-refresh the spinner already indicates loading,
                    // so we keep the existing list visible.
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
                    if (state.posts.isEmpty()) {
                        binding.rvPosts.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = getString(R.string.home_empty_posts)
                    } else {
                        binding.tvError.visibility = View.GONE
                        binding.rvPosts.visibility = View.VISIBLE
                        adapter.submitList(state.posts)
                    }
                }
                is NewsfeedUiState.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.rvPosts.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener { showFeatureInDevelopment() }
        binding.tvSeeAll.setOnClickListener { showFeatureInDevelopment() }
        binding.catVietnamese.setOnClickListener { showFeatureInDevelopment() }
        binding.catFastFood.setOnClickListener { showFeatureInDevelopment() }
        binding.catCoffee.setOnClickListener { showFeatureInDevelopment() }
        binding.catRestaurant.setOnClickListener { showFeatureInDevelopment() }
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
