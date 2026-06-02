package com.urmyfood.shop.presentation.main.posts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainPostsBinding
import com.urmyfood.shop.presentation.common.safeNavigate
import com.urmyfood.shop.presentation.main.posts.adapter.PostsAdapter

import org.json.JSONObject

class PostsFragment : Fragment(), PostsAdapter.PostActionListener {

    private var _binding: FragmentMainPostsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostsViewModel by viewModels()
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
        setupRecyclerView()
        setupSearch()
        setupListeners()
        observeData()
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
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postsAdapter.submitList(posts)
        }
    }

    // Helper to serialize ShopPost to String
    private fun serializePost(post: ShopPost): String {
        return JSONObject().apply {
            put("postId", post.postId)
            put("dishName", post.dishName)
            put("price", post.price)
            post.originalPrice?.let { put("originalPrice", it) }
            post.content?.let { put("content", it) }
            put("imageUrl", post.imageUrl)
            put("stock", post.stock)
            put("maxStock", post.maxStock)
            put("isActive", post.isActive)
            put("isFlashSale", post.isFlashSale)
            put("likeCount", post.likeCount)
            put("commentCount", post.commentCount)
            post.category?.let { put("category", it) }
        }.toString()
    }

    // PostActionListener callbacks
    override fun onSwitchToggle(postId: String, isActive: Boolean) {
        viewModel.toggleActive(postId)
    }

    override fun onEditClick(post: ShopPost) {
        val bundle = Bundle().apply {
            putString("postJson", serializePost(post))
        }
        findNavController().navigate(R.id.action_posts_to_createPost, bundle)
    }

    override fun onItemClick(post: ShopPost) {
        val bundle = Bundle().apply {
            putString("postJson", serializePost(post))
        }
        findNavController().navigate(R.id.action_posts_to_postDetail, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
