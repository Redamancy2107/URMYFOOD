package com.urmyfood.shop.presentation.main.account

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentShopProfileEditBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.domain.model.ShopCategory
import com.urmyfood.shop.domain.model.ShopProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ShopProfileEditFragment : Fragment() {

    private var _binding: FragmentShopProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShopProfileEditViewModel by viewModels {
        ServiceLocator.provideShopProfileEditViewModelFactory()
    }

    private var selectedLogoUri: Uri? = null
    private var selectedCoverUri: Uri? = null
    private var currentLogoUrl: String? = null
    private var currentCoverUrl: String? = null
    private var pickingCover = false
    private var selectedCategory: ShopCategory? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var hasPendingMapSelection = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        if (pickingCover) {
            selectedCoverUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.bg_food_banner)
                .error(R.drawable.bg_food_banner)
                .into(binding.ivCover)
        } else {
            selectedLogoUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(binding.ivAvatar)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShopProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupFragmentResultListener()
        observeViewModel()
        viewModel.loadProfileIfNeeded()
    }

    private fun observeViewModel() {
        viewModel.loadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileUiState.Loading -> renderLoading()
                is ProfileUiState.Success -> renderProfile(state.profile)
                is ProfileUiState.Error -> renderLoadError(state.message)
                is ProfileUiState.Idle -> Unit
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileEditUiState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = state.message
                }
                is ProfileEditUiState.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.profile_edit_save_success), Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh_profile", true)
                    findNavController().popBackStack()
                }
                is ProfileEditUiState.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = getString(R.string.profile_edit_save)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ProfileEditUiState.Idle -> Unit
            }
        }
    }

    private fun renderLoading() {
        Glide.with(this).clear(binding.ivCover)
        Glide.with(this).clear(binding.ivAvatar)
        setFormEnabled(false)

        val loadingText = getString(R.string.shop_profile_loading)
        binding.ivCover.setImageDrawable(null)
        binding.ivCover.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.profile_icon_bg_policy))
        binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
        binding.etShopName.setText(loadingText)
        binding.tvCategoryValue.text = loadingText
        binding.tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
        binding.etAddress.setText(loadingText)
        binding.etDescription.setText("")
        binding.etOpeningHours.setText(loadingText)
        binding.switchOpen.isChecked = false
        binding.btnSave.text = getString(R.string.profile_edit_save)
    }

    private fun renderProfile(profile: ShopProfile) {
        setFormEnabled(true)
        binding.etShopName.setText(profile.shopName)
        selectedCategory = categoryFromCode(profile.category)
        binding.tvCategoryValue.text = selectedCategory?.displayName ?: getString(R.string.shop_profile_category_hint)
        binding.tvCategoryValue.setTextColor(requireContext().getColor(
            if (selectedCategory == null) R.color.text_hint else R.color.text_primary
        ))
        if (!hasPendingMapSelection) {
            binding.etAddress.setText(profile.address)
            selectedLatitude = profile.latitude
            selectedLongitude = profile.longitude
        }
        binding.etDescription.setText(profile.description.orEmpty())
        binding.etOpeningHours.setText(profile.openingHours)
        binding.switchOpen.isChecked = profile.isOpen

        currentLogoUrl = profile.logoUrl
        currentCoverUrl = profile.coverUrl
        binding.ivAvatar.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        binding.ivCover.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        if (profile.logoUrl.isNullOrEmpty()) {
            binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
        } else {
            Glide.with(this)
                .load(profile.logoUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(binding.ivAvatar)
        }
        if (profile.coverUrl.isNullOrEmpty()) {
            binding.ivCover.setImageResource(R.drawable.bg_food_banner)
        } else {
            Glide.with(this)
                .load(profile.coverUrl)
                .placeholder(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.profile_icon_bg_policy)))
                .error(R.drawable.bg_food_banner)
                .into(binding.ivCover)
        }
    }

    private fun renderLoadError(message: String) {
        Glide.with(this).clear(binding.ivCover)
        Glide.with(this).clear(binding.ivAvatar)
        setFormEnabled(false)

        binding.ivCover.setImageDrawable(null)
        binding.ivCover.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.profile_icon_bg_policy))
        binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
        binding.etShopName.setText("")
        binding.tvCategoryValue.text = getString(R.string.shop_profile_category_hint)
        binding.tvCategoryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
        binding.etAddress.setText("")
        binding.etDescription.setText("")
        binding.etOpeningHours.setText("")
        binding.switchOpen.isChecked = false
        binding.btnSave.text = getString(R.string.profile_edit_save)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etShopName.isEnabled = enabled
        binding.rowCategory.isEnabled = enabled
        binding.etAddress.isEnabled = enabled
        binding.btnMapPicker.isEnabled = enabled
        binding.etDescription.isEnabled = enabled
        binding.etOpeningHours.isEnabled = enabled
        binding.switchOpen.isEnabled = enabled
        binding.btnEditAvatar.isEnabled = enabled
        binding.btnEditCover.isEnabled = enabled
        binding.btnChangePassword.isEnabled = enabled
        binding.btnSave.isEnabled = enabled
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnEditAvatar.setOnClickListener {
            pickingCover = false
            pickImageLauncher.launch("image/*")
        }

        binding.btnEditCover.setOnClickListener {
            pickingCover = true
            pickImageLauncher.launch("image/*")
        }

        binding.rowCategory.setOnClickListener {
            showCategoryPicker()
        }

        binding.btnMapPicker.setOnClickListener {
            findNavController().navigate(R.id.action_shopProfileEditFragment_to_mapPickerFragment)
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_shopProfileEditFragment_to_changePasswordFragment)
        }

        binding.btnSave.setOnClickListener {
            val logoImagePart = selectedLogoUri?.toMultipartPart("file")
            if (selectedLogoUri != null && logoImagePart == null) {
                return@setOnClickListener
            }
            val coverImagePart = selectedCoverUri?.toMultipartPart("file")
            if (selectedCoverUri != null && coverImagePart == null) {
                return@setOnClickListener
            }
            viewModel.updateProfile(
                shopName = binding.etShopName.text.toString(),
                logoUrl = selectedLogoUri?.toString() ?: currentLogoUrl,
                coverUrl = selectedCoverUri?.toString() ?: currentCoverUrl,
                category = selectedCategory?.name.orEmpty(),
                address = binding.etAddress.text.toString(),
                latitude = selectedLatitude,
                longitude = selectedLongitude,
                description = binding.etDescription.text.toString(),
                openingHours = binding.etOpeningHours.text.toString(),
                isOpen = binding.switchOpen.isChecked,
                logoImagePart = logoImagePart,
                coverImagePart = coverImagePart
            )
        }
    }

    private fun Uri.toMultipartPart(fieldName: String): MultipartBody.Part? {
        return try {
            val mimeType = requireContext().contentResolver.getType(this) ?: "image/jpeg"
            val bytes = requireContext().contentResolver.openInputStream(this)?.use { it.readBytes() }
                ?: return null
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(fieldName, "profile-image", body)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("map_result") { _, bundle ->
            val address = bundle.getString("address", "")
            selectedLatitude = bundle.getDouble("lat")
            selectedLongitude = bundle.getDouble("lng")
            hasPendingMapSelection = true
            binding.etAddress.setText(address)
        }
    }

    private fun showCategoryPicker() {
        val categories = ShopCategory.entries.toTypedArray()
        val names = categories.map { it.displayName }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.shop_profile_category_hint)
            .setItems(names) { _, which ->
                selectedCategory = categories[which]
                binding.tvCategoryValue.text = categories[which].displayName
                binding.tvCategoryValue.setTextColor(requireContext().getColor(R.color.text_primary))
            }
            .show()
    }

    private fun categoryFromCode(categoryCode: String): ShopCategory? {
        return ShopCategory.entries.firstOrNull { it.name == categoryCode || it.displayName == categoryCode }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
