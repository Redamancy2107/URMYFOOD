package com.urmyfood.shop.presentation.main.posts

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentCreatePostBinding
import com.urmyfood.shop.di.ServiceLocator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreatePostViewModel

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadImageFromUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getString("postId")
        viewModel = ViewModelProvider(this, ServiceLocator.provideCreatePostViewModelFactory(postId))
            .get(CreatePostViewModel::class.java)

        setupToolbar()
        setupCategorySpinner()
        setupTextWatchers()
        setupListeners()
        observeViewModel()

        if (viewModel.isEditMode) {
            binding.toolbar.title = "Chỉnh sửa bài đăng"
            binding.btnSave.text = "Lưu thay đổi"
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            viewModel.categories
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupTextWatchers() {
        binding.etDishName.addTextChangedListener(simpleWatcher { viewModel.setDishName(it) })
        binding.etPrice.addTextChangedListener(simpleWatcher { viewModel.setPrice(it) })
        binding.etOriginalPrice.addTextChangedListener(simpleWatcher { viewModel.setOriginalPrice(it) })
        binding.etDescription.addTextChangedListener(simpleWatcher { viewModel.setDescription(it) })
    }

    private fun simpleWatcher(action: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { action(s?.toString().orEmpty()) }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun setupListeners() {
        binding.switchFlashSale.setOnCheckedChangeListener { _, isChecked -> viewModel.setFlashSale(isChecked) }
        binding.btnDecrement.setOnClickListener { viewModel.decrementStock() }
        binding.btnIncrement.setOnClickListener { viewModel.incrementStock() }
        binding.btnAddImage.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnSave.setOnClickListener {
            viewModel.setCategory(binding.spinnerCategory.selectedItem?.toString() ?: "Món chính")
            viewModel.savePost()
        }

        binding.tvStockCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.toIntOrNull()?.let { viewModel.setStock(it) }
            }
        })

        binding.tvStockCount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = binding.tvStockCount.text.toString()
                if (input.isEmpty() || input.toIntOrNull() == null) {
                    binding.tvStockCount.setText((viewModel.stockCount.value ?: 10).toString())
                }
            }
        }
    }

    private fun copyUriToCacheFile(uri: Uri): java.io.File? {
        val context = requireContext()
        val contentResolver = context.contentResolver
        val tempFile = java.io.File(context.cacheDir, "temp_post_image_${System.currentTimeMillis()}.jpg")
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                java.io.FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageFromUri(uri: Uri) {
        val cacheFile = copyUriToCacheFile(uri) ?: return
        val cacheFileUri = Uri.fromFile(cacheFile)
        val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = cacheFile.readBytes()
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", cacheFile.name, requestBody)
        viewModel.uploadImage(part)

        Glide.with(this)
            .load(cacheFileUri)
            .placeholder(R.drawable.bg_food_banner)
            .into(binding.ivPostImage)
        binding.ivPostImage.visibility = View.VISIBLE
    }

    private fun observeViewModel() {
        viewModel.stockCount.observe(viewLifecycleOwner) { count ->
            val stockStr = count.toString()
            if (binding.tvStockCount.text.toString() != stockStr) {
                binding.tvStockCount.setText(stockStr)
            }
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty() && binding.ivPostImage.tag != url) {
                binding.ivPostImage.tag = url
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(this).load(url).placeholder(R.drawable.bg_food_banner).into(binding.ivPostImage)
            }
        }

        viewModel.category.observe(viewLifecycleOwner) { cat ->
            val pos = viewModel.categories.indexOf(cat)
            if (pos != -1 && binding.spinnerCategory.selectedItemPosition != pos) {
                binding.spinnerCategory.setSelection(pos)
            }
        }

        viewModel.dishName.observe(viewLifecycleOwner) { name ->
            if (binding.etDishName.text.toString() != name) binding.etDishName.setText(name)
        }

        viewModel.price.observe(viewLifecycleOwner) { p ->
            if (binding.etPrice.text.toString() != p) binding.etPrice.setText(p)
        }

        viewModel.originalPrice.observe(viewLifecycleOwner) { p ->
            if (binding.etOriginalPrice.text.toString() != p) binding.etOriginalPrice.setText(p)
        }

        viewModel.description.observe(viewLifecycleOwner) { d ->
            if (binding.etDescription.text.toString() != d) binding.etDescription.setText(d)
        }

        viewModel.isFlashSale.observe(viewLifecycleOwner) { b ->
            if (binding.switchFlashSale.isChecked != b) binding.switchFlashSale.isChecked = b
        }

        viewModel.imageUploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ImageUploadState.Uploading -> binding.btnAddImage.isEnabled = false
                is ImageUploadState.Success -> binding.btnAddImage.isEnabled = true
                is ImageUploadState.Error -> {
                    binding.btnAddImage.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> binding.btnAddImage.isEnabled = true
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            when (result) {
                is Result.Success -> {
                    val msg = if (viewModel.isEditMode) "Cập nhật bài đăng thành công!" else "Tạo bài đăng thành công!"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearSaveResult()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("post_updated", true)
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearSaveResult()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
