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
            showChangePasswordDialog()
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

                binding.ivAdminAvatar.setImageResource(R.drawable.ic_logo_admin)
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Không thể tải hồ sơ: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAdminProfile() {
        val updates = mapOf(
            "full_name" to binding.etFullName.text.toString(),
            "work_email" to binding.etEmail.text.toString(),
            "phone_number" to binding.etPhone.text.toString(),
            "position" to binding.etPosition.text.toString(),
            "short_bio" to binding.etBio.text.toString()
        )
        
        lifecycleScope.launch {
            val result = repository.updateAdminProfile(updates)
            result.onSuccess { profile ->
                Toast.makeText(requireContext(), "Đã lưu hồ sơ thành công", Toast.LENGTH_SHORT).show()
                binding.tvAdminName.text = profile.fullName ?: "Admin"
                binding.tvAdminRole.text = profile.position ?: "Hệ thống Quản trị"
                // Update header in DashboardFragment
                (parentFragment as? DashboardFragment)?.updateAdminHeader(profile.fullName ?: "Admin", profile.avatarUrl)
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
                // Update header in DashboardFragment
                (parentFragment as? DashboardFragment)?.updateAdminHeader(profile.fullName ?: "Admin", profile.avatarUrl)
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Tải ảnh đại diện thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val etCurrentPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_current_password)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_new_password)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_confirm_password)
        val tvError = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_error)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)
        val btnSubmit = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_submit)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            val currentPass = etCurrentPassword.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (currentPass.isEmpty()) {
                tvError.text = "Vui lòng nhập mật khẩu hiện tại"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (newPass.isEmpty()) {
                tvError.text = "Vui lòng nhập mật khẩu mới"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (newPass.length < 6) {
                tvError.text = "Mật khẩu mới phải từ 6 ký tự trở lên"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                tvError.text = "Xác nhận mật khẩu không trùng khớp"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            btnSubmit.isEnabled = false
            btnSubmit.text = "Đang lưu..."

            lifecycleScope.launch {
                val result = repository.changePassword(
                    com.urmyfood.admin.data.model.ChangePasswordRequest(
                        currentPassword = currentPass,
                        newPassword = newPass
                    )
                )
                result.onSuccess {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }.onFailure { error ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Cập nhật"
                    tvError.text = error.message ?: "Đổi mật khẩu thất bại"
                    tvError.visibility = View.VISIBLE
                }
            }
        }

        dialog.show()
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
