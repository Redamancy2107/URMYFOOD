package com.urmyfood.shop.presentation.main.posts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentCreatePostBinding
import org.json.JSONObject

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels()

    private var selectedImageUri: android.net.Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivPostImage.visibility = View.VISIBLE
            com.bumptech.glide.Glide.with(this)
                .load(it)
                .placeholder(R.drawable.bg_food_banner)
                .error(R.drawable.bg_food_banner)
                .into(binding.ivPostImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupCategorySpinner()
        
        // Parse editing post argument if any
        arguments?.getString("postJson")?.let { jsonStr ->
            try {
                val json = JSONObject(jsonStr)
                val post = ShopPost(
                    postId = json.getString("postId"),
                    dishName = json.getString("dishName"),
                    price = json.getLong("price"),
                    originalPrice = if (json.has("originalPrice")) json.getLong("originalPrice") else null,
                    content = if (json.has("content")) json.getString("content") else null,
                    imageUrl = json.getString("imageUrl"),
                    stock = json.getInt("stock"),
                    maxStock = json.getInt("maxStock"),
                    isActive = json.getBoolean("isActive"),
                    isFlashSale = json.getBoolean("isFlashSale"),
                    likeCount = json.getInt("likeCount"),
                    commentCount = json.getInt("commentCount"),
                    category = if (json.has("category")) json.getString("category") else null
                )
                viewModel.setupForEdit(post)
                if (post.imageUrl.isNotEmpty()) {
                    binding.ivPostImage.visibility = View.VISIBLE
                    com.bumptech.glide.Glide.with(this)
                        .load(post.imageUrl)
                        .placeholder(R.drawable.bg_food_banner)
                        .error(R.drawable.bg_food_banner)
                        .into(binding.ivPostImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setupTextWatchers()
        setupListeners()
        observeViewModel()
        
        // Populate inputs if in edit mode
        if (viewModel.isEditMode) {
            binding.toolbar.title = "Chỉnh sửa bài đăng"
            binding.btnSave.text = "Lưu thay đổi"
            binding.etDishName.setText(viewModel.dishName.value)
            binding.etPrice.setText(viewModel.price.value)
            binding.etOriginalPrice.setText(viewModel.originalPrice.value)
            binding.etDescription.setText(viewModel.description.value)
            binding.switchAvailable.isChecked = viewModel.isAvailable.value == true
            binding.switchFlashSale.isChecked = viewModel.isFlashSale.value == true
            
            val categoryPos = viewModel.categories.indexOf(viewModel.category.value)
            if (categoryPos != -1) {
                binding.spinnerCategory.setSelection(categoryPos)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            viewModel.categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupTextWatchers() {
        binding.etDishName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setDishName(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setPrice(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etOriginalPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setOriginalPrice(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setDescription(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        binding.switchAvailable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAvailable(isChecked)
        }

        binding.switchFlashSale.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFlashSale(isChecked)
        }

        binding.btnDecrement.setOnClickListener {
            viewModel.decrementStock()
        }

        binding.btnIncrement.setOnClickListener {
            viewModel.incrementStock()
        }

        binding.btnSave.setOnClickListener {
            val error = viewModel.validate()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            } else {
                val dishName = viewModel.dishName.value.orEmpty()
                val price = viewModel.price.value.orEmpty().toLongOrNull() ?: 0L
                val originalPrice = viewModel.originalPrice.value.orEmpty().toLongOrNull()
                val content = viewModel.description.value.orEmpty()
                val isAvailable = viewModel.isAvailable.value ?: true
                val isFlashSale = viewModel.isFlashSale.value ?: false
                val stock = viewModel.stockCount.value ?: 10
                val category = binding.spinnerCategory.selectedItem?.toString() ?: "Món chính"
                
                val imageUrl = selectedImageUri?.toString() ?: (if (viewModel.isEditMode) {
                    PostsViewModel.allPosts.find { it.postId == viewModel.editingPostId }?.imageUrl ?: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                } else {
                    "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                })

                if (viewModel.isEditMode) {
                    val index = PostsViewModel.allPosts.indexOfFirst { it.postId == viewModel.editingPostId }
                    if (index != -1) {
                        val existing = PostsViewModel.allPosts[index]
                        PostsViewModel.allPosts[index] = existing.copy(
                            dishName = dishName,
                            price = price,
                            originalPrice = originalPrice,
                            content = content,
                            imageUrl = imageUrl,
                            stock = stock,
                            isActive = isAvailable,
                            isFlashSale = isFlashSale,
                            category = category
                        )
                    }
                } else {
                    val newPost = ShopPost(
                        postId = "post_${System.currentTimeMillis()}",
                        dishName = dishName,
                        price = price,
                        originalPrice = originalPrice,
                        content = content,
                        imageUrl = imageUrl,
                        stock = stock,
                        maxStock = stock,
                        isActive = isAvailable,
                        isFlashSale = isFlashSale,
                        category = category,
                        likeCount = 0,
                        commentCount = 0
                    )
                    PostsViewModel.allPosts.add(newPost)
                }

                val message = if (viewModel.isEditMode) "Cập nhật bài đăng thành công!" else "Tạo bài đăng thành công!"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Text watcher for stock edit box
        binding.tvStockCount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val input = s?.toString().orEmpty()
                val parsed = input.toIntOrNull()
                if (parsed != null) {
                    viewModel.setStock(parsed)
                }
            }
        })

        // Reset text if left empty on focus lost
        binding.tvStockCount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = binding.tvStockCount.text.toString()
                if (input.isEmpty() || input.toIntOrNull() == null) {
                    binding.tvStockCount.setText((viewModel.stockCount.value ?: 10).toString())
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.stockCount.observe(viewLifecycleOwner) { count ->
            val currentInput = binding.tvStockCount.text.toString()
            val stockStr = count.toString()
            if (currentInput != stockStr) {
                binding.tvStockCount.setText(stockStr)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
