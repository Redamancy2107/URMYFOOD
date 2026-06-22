package com.urmyfood.admin.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.urmyfood.admin.R
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val repository = AdminRepository()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data ?: return@registerForActivityResult
            uploadAvatarFile(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadAdminProfile()

        binding.btnSaveProfile.setOnClickListener {
            saveAdminProfile()
        }
        
        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.fabEditAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }
    }

    private fun loadAdminProfile() {
        lifecycleScope.launch {
            val result = repository.getAdminProfile()
            result.onSuccess { profile ->
                binding.etFullName.setText(profile.fullName)
                binding.etEmail.setText(profile.workEmail)
                binding.etPhone.setText(profile.phoneNumber ?: "")
                binding.etPosition.setText(profile.position ?: "")
                binding.etBio.setText(profile.shortBio ?: "")
                binding.tv2faStatus.text = if (profile.is2FaEnabled) "ĐANG BẬT" else "ĐANG TẮT"
                
                binding.tvAdminName.text = profile.fullName ?: "Admin"
                binding.tvAdminRole.text = profile.position ?: "Hệ thống Quản trị"

                profile.avatarUrl?.let { url ->
                    Glide.with(this@SettingsFragment)
                        .load(url)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .into(binding.ivAdminAvatar)
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Không thể tải hồ sơ: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAdminProfile() {
        val updates = mapOf(
            "fullName" to binding.etFullName.text.toString(),
            "workEmail" to binding.etEmail.text.toString(),
            "phoneNumber" to binding.etPhone.text.toString(),
            "position" to binding.etPosition.text.toString(),
            "shortBio" to binding.etBio.text.toString()
        )
        
        lifecycleScope.launch {
            val result = repository.updateAdminProfile(updates)
            result.onSuccess { profile ->
                Toast.makeText(requireContext(), "Đã lưu hồ sơ thành công", Toast.LENGTH_SHORT).show()
                binding.tvAdminName.text = profile.fullName ?: "Admin"
                binding.tvAdminRole.text = profile.position ?: "Hệ thống Quản trị"
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Lưu hồ sơ thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadAvatarFile(uri: Uri) {
        val file = getFileFromUri(uri) ?: return
        lifecycleScope.launch {
            val result = repository.uploadAvatar(file)
            result.onSuccess { profile ->
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show()
                profile.avatarUrl?.let { url ->
                    Glide.with(this@SettingsFragment)
                        .load(url)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .into(binding.ivAdminAvatar)
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Tải ảnh đại diện thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val context = context ?: return null
        val tempFile = File(context.cacheDir, "temp_avatar.jpg")
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
