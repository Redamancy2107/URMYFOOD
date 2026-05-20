package com.urmyfood.user.presentation.main.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentProfileEditBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Màn hình Chỉnh sửa thông tin cá nhân.
 * Đọc dữ liệu từ backend (Supabase), cho phép cập nhật avatar và thông tin cá nhân.
 */
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels {
        ServiceLocator.provideProfileEditViewModelFactory()
    }

    private var selectedAvatarUri: Uri? = null
    private var currentAvatarUrl: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(binding.ivAvatar)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
        viewModel.loadProfile()
    }

    private fun observeViewModel() {
        viewModel.loadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileUiState.Loading -> {
                    // Loading UI
                }
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    binding.etFullName.setText(profile.fullName)
                    binding.etEmail.setText(profile.email)
                    binding.etPhone.setText(profile.phone ?: "")
                    
                    // Email should be read-only since it is fetched from Supabase
                    binding.etEmail.isEnabled = false
                    
                    currentAvatarUrl = profile.avatarUrl
                    if (!profile.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profile.avatarUrl)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .error(R.drawable.ic_person_placeholder)
                            .into(binding.ivAvatar)
                    }
                }
                is ProfileUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ProfileUiState.Idle -> Unit
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileEditUiState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Đang lưu..."
                }
                is ProfileEditUiState.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.profile_edit_save_success), Toast.LENGTH_SHORT).show()
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

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnEditAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.changePasswordFragment)
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.profile_edit_name_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val avatarUrl = selectedAvatarUri?.toString() ?: currentAvatarUrl
            viewModel.updateProfile(name, phone, avatarUrl)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
