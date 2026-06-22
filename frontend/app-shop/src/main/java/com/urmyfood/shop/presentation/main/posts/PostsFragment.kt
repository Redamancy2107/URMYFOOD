package com.urmyfood.shop.presentation.main.posts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainPostsBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.presentation.common.safeNavigate
import com.urmyfood.shop.presentation.main.posts.adapter.PostsAdapter

class PostsFragment : Fragment(), PostsAdapter.PostActionListener {

    private var _binding: FragmentMainPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PostsViewModel
    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ServiceLocator.providePostsViewModelFactory())
            .get(PostsViewModel::class.java)
        setupRecyclerView()
        setupSearch()
        setupListeners()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPosts()
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(this)
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterPosts(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        binding.btnCreatePost.setOnClickListener {
            findNavController().safeNavigate(R.id.action_posts_to_createPost)
        }
    }

    private fun observeData() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PostsUiState.Loading -> { /* show shimmer if available */ }
                is PostsUiState.Success -> postsAdapter.submitList(state.posts)
                is PostsUiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            if (result is com.urmyfood.shared.domain.model.Result.Error) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            }
            viewModel.clearDeleteResult()
        }
    }

    override fun onSwitchToggle(postId: String, isActive: Boolean) {
        viewModel.toggleActive(postId, isActive)
    }

    override fun onEditClick(post: Post) {
        val bundle = Bundle().apply { putString("postId", post.postId) }
        findNavController().navigate(R.id.action_posts_to_createPost, bundle)
    }

    override fun onItemClick(post: Post) {
        val bundle = Bundle().apply { putString("postId", post.postId) }
        findNavController().navigate(R.id.action_posts_to_postDetail, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
