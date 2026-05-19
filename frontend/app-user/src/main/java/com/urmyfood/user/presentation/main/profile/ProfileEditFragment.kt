package com.urmyfood.user.presentation.main.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentProfileEditBinding
import com.urmyfood.user.di.ServiceLocator
import java.util.Calendar

/**
 * Màn hình Chỉnh sửa thông tin cá nhân.
 * Đọc dữ liệu từ TokenManager, cho phép sửa và lưu cục bộ.
 */
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    private val tokenManager by lazy { ServiceLocator.tokenManager }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCurrentInfo()
        setupListeners()
    }

    private fun loadCurrentInfo() {
        binding.etFullName.setText(tokenManager.getFullName() ?: "")
        // Email and phone loaded from SharedPreferences if available
        val prefs = requireContext().getSharedPreferences("user_profile", 0)
        binding.etEmail.setText(prefs.getString("email", ""))
        binding.etPhone.setText(prefs.getString("phone", ""))
        binding.etDob.setText(prefs.getString("dob", ""))
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.etDob.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etDob.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.changePasswordFragment)
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.profile_edit_name_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Lưu cục bộ vào SharedPreferences
            requireContext().getSharedPreferences("user_profile", 0).edit()
                .putString("fullName", name)
                .putString("email", binding.etEmail.text.toString().trim())
                .putString("phone", binding.etPhone.text.toString().trim())
                .putString("dob", binding.etDob.text.toString().trim())
                .apply()

            Toast.makeText(requireContext(), getString(R.string.profile_edit_save_success), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
