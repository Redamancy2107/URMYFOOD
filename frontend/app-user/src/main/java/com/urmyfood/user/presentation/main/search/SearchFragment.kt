package com.urmyfood.user.presentation.main.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainSearchBinding
import com.urmyfood.user.presentation.main.home.FoodPostAdapter
import com.urmyfood.user.presentation.main.home.OrderBottomSheetFragment
import com.urmyfood.user.presentation.main.home.QuickCommentFragment
import com.urmyfood.user.presentation.main.home.ShareBottomSheetFragment
import androidx.navigation.fragment.findNavController

class SearchFragment : Fragment() {

    private var _binding: FragmentMainSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideSearchViewModelFactory()
    }

    private val searchAdapter = FoodPostAdapter()
    private val recentSearchAdapter = RecentSearchAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupResultsRecyclerView()
        setupRecentRecyclerView()
        setupSearchInput()
        setupScrollListener()
        setupClickListeners()
        observeUiState()
        observeQuery()
        observeRecentSearches()
        observeLoadingMore()
        observeLikeError()
        observeFollowError()
        observeSaveError()
        observeFollowEvents()
    }

    private fun setupRecentRecyclerView() {
        binding.rvRecentSearches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentSearches.adapter = recentSearchAdapter
        recentSearchAdapter.onQueryClick = { query ->
            viewModel.selectRecentSearch(query)
        }
        recentSearchAdapter.onRemoveClick = { query ->
            viewModel.removeRecentSearch(query)
        }
    }

    private fun setupResultsRecyclerView() {
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
        searchAdapter.onCommentClick = { postId ->
            QuickCommentFragment.newInstance(postId)
                .show(childFragmentManager, QuickCommentFragment.TAG)
        }
        searchAdapter.onShareClick = {
            val sheet = ShareBottomSheetFragment()
            sheet.show(childFragmentManager, ShareBottomSheetFragment.TAG)
        }
        searchAdapter.onOrderClick = { post ->
            val sheet = OrderBottomSheetFragment(post)
            sheet.show(childFragmentManager, OrderBottomSheetFragment.TAG)
        }
        searchAdapter.onLikeClick = { post ->
            viewModel.toggleLike(post.postId, post.isLiked)
        }
        searchAdapter.onFollowClick = { post ->
            viewModel.toggleFollow(post.shopAccountId, post.isFollowingShop)
        }
        searchAdapter.onSaveClick = { post ->
            viewModel.toggleSave(post.postId, post.isSaved)
        }
        searchAdapter.onShopClick = { post ->
            val bundle = Bundle().apply {
                putString("shopName", post.shopName)
                putString("shopAvatarUrl", post.shopAvatarUrl)
                putLong("shopId", post.shopAccountId)
            }
            findNavController().navigate(R.id.shopProfileFragment, bundle)
        }
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s?.toString() ?: "")
            }
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.submitSearch(binding.etSearch.text?.toString() ?: "")
                true
            } else {
                false
            }
        }
    }

    private fun setupScrollListener() {
        binding.rvSearchResults.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = lm.findLastVisibleItemPosition()
                if (lastVisible >= (rv.adapter?.itemCount ?: 0) - 3) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchViewModel.UiState.Idle -> {
                    binding.layoutSearchResults.visibility = View.GONE
                    binding.scrollViewContent.visibility = View.VISIBLE
                    binding.pbSearchLoading.visibility = View.GONE
                }
                is SearchViewModel.UiState.Loading -> {
                    binding.scrollViewContent.visibility = View.GONE
                    binding.layoutSearchResults.visibility = View.VISIBLE
                    binding.pbSearchLoading.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                    binding.tvNoResults.visibility = View.GONE
                }
                is SearchViewModel.UiState.Success -> {
                    binding.scrollViewContent.visibility = View.GONE
                    binding.layoutSearchResults.visibility = View.VISIBLE
                    binding.pbSearchLoading.visibility = View.GONE
                    if (state.posts.isEmpty()) {
                        binding.tvNoResults.setText(R.string.search_no_results)
                        binding.tvNoResults.visibility = View.VISIBLE
                        binding.rvSearchResults.visibility = View.GONE
                    } else {
                        binding.tvNoResults.visibility = View.GONE
                        binding.rvSearchResults.visibility = View.VISIBLE
                        searchAdapter.submitList(state.posts) {
                            if (state.page == 0) {
                                binding.rvSearchResults.scrollToPosition(0)
                            }
                        }
                    }
                }
                is SearchViewModel.UiState.Error -> {
                    binding.scrollViewContent.visibility = View.GONE
                    binding.layoutSearchResults.visibility = View.VISIBLE
                    binding.pbSearchLoading.visibility = View.GONE
                    binding.tvNoResults.text = state.message
                    binding.tvNoResults.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                }
            }
        }
    }

    private fun observeQuery() {
        viewModel.query.observe(viewLifecycleOwner) { query ->
            if (binding.etSearch.text?.toString() != query) {
                binding.etSearch.setText(query)
                binding.etSearch.setSelection(query.length)
            }
        }
    }

    private fun observeRecentSearches() {
        viewModel.recentSearches.observe(viewLifecycleOwner) { searches ->
            recentSearchAdapter.submitList(searches)
            binding.tvClearAll.visibility = if (searches.isEmpty()) View.GONE else View.VISIBLE
            binding.rvRecentSearches.visibility = if (searches.isEmpty()) View.GONE else View.VISIBLE
            binding.tvNoRecentSearches.visibility = if (searches.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observeLoadingMore() {
        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
            binding.pbSearchLoadingMore.visibility = if (loading) View.VISIBLE else View.GONE
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

    private fun observeSaveError() {
        viewModel.saveError.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveError()
        }
    }

    private fun observeFollowEvents() {
        com.urmyfood.user.di.ServiceLocator.shopFollowEvent.observe(viewLifecycleOwner) { event ->
            val (shopId, isFollowing) = event
            viewModel.updateFollowStateForShop(shopId, isFollowing)
        }
        com.urmyfood.user.di.ServiceLocator.postSavedEvent.observe(viewLifecycleOwner) { event ->
            val (postId, isSaved) = event
            viewModel.updateSavedState(postId, isSaved)
        }
    }

    private fun setupClickListeners() {
        binding.tvClearAll.setOnClickListener { viewModel.clearRecentSearches() }
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
