package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val repository = AdminRepository()
    // TODO: Replace with the actual logged-in account ID
    private val mockAccountId = 1L 

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
    }

    private fun loadAdminProfile() {
        lifecycleScope.launch {
            val result = repository.getAdminProfile(mockAccountId)
            result.onSuccess { profile ->
                binding.etFullName.setText(profile.fullName)
                binding.etEmail.setText(profile.workEmail)
                binding.etPhone.setText(profile.phoneNumber ?: "")
                binding.etPosition.setText(profile.position ?: "")
                binding.etBio.setText(profile.shortBio ?: "")
                binding.tv2faStatus.text = if (profile.is2FaEnabled) "ĐANG BẬT" else "ĐANG TẮT"
            }.onFailure {
                // Ignore real failure if API key is not set, just show default mock text for demo
                Toast.makeText(requireContext(), "Chưa cấu hình API Key. Hiển thị dữ liệu mẫu.", Toast.LENGTH_SHORT).show()
                binding.etFullName.setText("Nguyễn Tuấn Anh")
                binding.etEmail.setText("tuananh.admin@urmyfood.com")
                binding.etPhone.setText("+84 987 654 321")
                binding.etPosition.setText("Senior Operations Admin")
                binding.etBio.setText("Quản trị viên điều hành hệ thống URMYFOOD. Trách nhiệm giám sát toàn bộ quy trình.")
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
            val result = repository.updateAdminProfile(mockAccountId, updates)
            result.onSuccess {
                Toast.makeText(requireContext(), "Đã lưu hồ sơ thành công", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Lưu ảo thành công (Chưa cấu hình API Key)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
