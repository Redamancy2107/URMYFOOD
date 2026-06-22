package com.urmyfood.user.presentation.main.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainFavoritesBinding
import com.urmyfood.user.presentation.main.home.FoodPostAdapter
import com.urmyfood.user.presentation.main.home.OrderBottomSheetFragment
import com.urmyfood.user.presentation.main.home.QuickCommentFragment

class FavoritesFragment : Fragment() {

    private var _binding: FragmentMainFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideFavoritesViewModelFactory()
    }

    private val adapter = FoodPostAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        observeSharedEvents()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSavedPosts()
    }

    private fun setupRecyclerView() {
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter

        adapter.onLikeClick = { post -> viewModel.toggleLike(post) }
        adapter.onFollowClick = { post -> viewModel.toggleFollow(post) }
        adapter.onSaveClick = { post -> viewModel.toggleSave(post) }
        adapter.onOrderClick = { post ->
            OrderBottomSheetFragment(post).show(childFragmentManager, OrderBottomSheetFragment.TAG)
        }
        adapter.onCommentClick = { postId ->
            QuickCommentFragment.newInstance(postId).show(childFragmentManager, QuickCommentFragment.TAG)
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

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesUiState.Loading -> Unit
                is FavoritesUiState.Success -> render(state.posts)
                is FavoritesUiState.Error -> {
                    render(emptyList())
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message ?: return@observe
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    private fun observeSharedEvents() {
        com.urmyfood.user.di.ServiceLocator.postCommentEvent.observe(viewLifecycleOwner) { postId ->
            viewModel.incrementComment(postId)
        }
        com.urmyfood.user.di.ServiceLocator.postLikeEvent.observe(viewLifecycleOwner) { event ->
            val (postId, likeData) = event
            val (isLiked, likeCount) = likeData
            viewModel.updateLike(postId, isLiked, likeCount)
        }
        com.urmyfood.user.di.ServiceLocator.shopFollowEvent.observe(viewLifecycleOwner) { event ->
            val (shopId, isFollowing) = event
            viewModel.updateFollow(shopId, isFollowing)
        }
        com.urmyfood.user.di.ServiceLocator.postSavedEvent.observe(viewLifecycleOwner) { event ->
            val (postId, isSaved) = event
            viewModel.updateSaved(postId, isSaved)
        }
    }

    private fun render(posts: List<com.urmyfood.user.domain.model.FoodPost>) {
        if (posts.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvFavorites.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvFavorites.visibility = View.VISIBLE
            adapter.submitList(posts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
