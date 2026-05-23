package com.urmyfood.user.presentation.main.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainFavoritesBinding
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.presentation.main.home.FoodPostAdapter
import com.urmyfood.user.presentation.main.home.OrderBottomSheetFragment
import com.urmyfood.user.presentation.main.home.QuickCommentFragment
import com.urmyfood.user.presentation.main.home.ShareBottomSheetFragment
import kotlinx.coroutines.launch

/**
 * Favorites screen fragment.
 * Displays the list of user's saved food posts.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentMainFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideFavoritesViewModelFactory()
    }

    private val favoritesManager = com.urmyfood.user.di.ServiceLocator.favoritesManager
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
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter

        adapter.checkIsBookmarked = { post ->
            favoritesManager.isFavorite(post.postId)
        }

        adapter.onLikeClick = { post ->
            val currentCount = post.likeCount
            val optimisticCount = if (post.isLiked) (currentCount - 1).coerceAtLeast(0) else currentCount + 1
            val optimisticPost = post.copy(isLiked = !post.isLiked, likeCount = optimisticCount)
            favoritesManager.updateFavorite(optimisticPost)
            loadFavorites()

            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = com.urmyfood.user.di.ServiceLocator.toggleLikeUseCase(post.postId, post.isLiked)) {
                    is Result.Success -> {
                        favoritesManager.updateFavorite(
                            optimisticPost.copy(
                                isLiked = result.data.isLiked,
                                likeCount = result.data.likeCount
                            )
                        )
                        loadFavorites()
                    }
                    is Result.Error -> {
                        favoritesManager.updateFavorite(post.copy(isLiked = post.isLiked, likeCount = currentCount))
                        loadFavorites()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        adapter.onSaveClick = { post ->
            val isSaved = favoritesManager.toggleFavorite(post)
            val msg = if (isSaved) "Đã lưu bài viết" else "Đã bỏ lưu bài viết"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            loadFavorites()
        }

        adapter.onOrderClick = { post ->
            val orderSheet = OrderBottomSheetFragment(post)
            orderSheet.show(childFragmentManager, OrderBottomSheetFragment.TAG)
        }

        adapter.onCommentClick = { postId ->
            QuickCommentFragment.newInstance(postId)
                .show(childFragmentManager, QuickCommentFragment.TAG)
        }

        adapter.onShareClick = {
            val shareSheet = ShareBottomSheetFragment()
            shareSheet.show(childFragmentManager, ShareBottomSheetFragment.TAG)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadFavorites() {
        val list = favoritesManager.getFavorites()
        if (list.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvFavorites.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvFavorites.visibility = View.VISIBLE
            adapter.submitList(list)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
