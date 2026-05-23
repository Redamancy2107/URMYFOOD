package com.urmyfood.user.presentation.main.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainSearchBinding
import com.urmyfood.user.presentation.main.home.FoodPostAdapter
import com.urmyfood.user.presentation.main.home.OrderBottomSheetFragment
import com.urmyfood.user.presentation.main.home.QuickCommentFragment
import com.urmyfood.user.presentation.main.home.ShareBottomSheetFragment

class SearchFragment : Fragment() {

    private var _binding: FragmentMainSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideSearchViewModelFactory()
    }

    private val searchAdapter = FoodPostAdapter()

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
        setupSearchInput()
        setupScrollListener()
        setupClickListeners()
        observeUiState()
        observeLoadingMore()
        observeLikeError()
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
        searchAdapter.onSaveClick = { showFeatureInDevelopment() }
        searchAdapter.checkIsBookmarked = { false }
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s?.toString() ?: "")
            }
        })

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            viewModel.search(binding.etSearch.text?.toString() ?: "")
            true
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
                        binding.tvNoResults.visibility = View.VISIBLE
                        binding.rvSearchResults.visibility = View.GONE
                    } else {
                        binding.tvNoResults.visibility = View.GONE
                        binding.rvSearchResults.visibility = View.VISIBLE
                        searchAdapter.submitList(state.posts)
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

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener { showFeatureInDevelopment() }
        binding.tvClearAll.setOnClickListener { showFeatureInDevelopment() }
        binding.catSeafood.setOnClickListener { showFeatureInDevelopment() }
        binding.catSnacks.setOnClickListener { showFeatureInDevelopment() }
        binding.catEatclean.setOnClickListener { showFeatureInDevelopment() }
        binding.catDrinks.setOnClickListener { showFeatureInDevelopment() }
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
