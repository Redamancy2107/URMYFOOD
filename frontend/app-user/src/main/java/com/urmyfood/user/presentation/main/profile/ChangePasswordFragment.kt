package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentChangePasswordBinding

/**
 * Màn hình Đổi mật khẩu.
 * 3 ô nhập với nút ẩn/hiện mật khẩu, validation khớp & độ dài tối thiểu.
 */
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // Toggle visibility for each password field
        setupToggle(binding.etCurrentPassword, binding.btnToggleCurrent)
        setupToggle(binding.etNewPassword, binding.btnToggleNew)
        setupToggle(binding.etConfirmPassword, binding.btnToggleConfirm)

        binding.tvForgotPassword.setOnClickListener {
            try {
                val parentNavController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                parentNavController.navigate(R.id.forgotPasswordFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnConfirm.setOnClickListener { validateAndSubmit() }
    }

    private fun setupToggle(editText: EditText, toggleBtn: ImageButton) {
        toggleBtn.setOnClickListener {
            val isHidden = editText.transformationMethod is PasswordTransformationMethod
            editText.transformationMethod = if (isHidden) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            toggleBtn.setImageResource(if (isHidden) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
            editText.setSelection(editText.text.length)
        }
    }

    private fun validateAndSubmit() {
        val current = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        when {
            current.isEmpty() || newPass.isEmpty() || confirm.isEmpty() ->
                toast(R.string.change_password_empty)
            newPass.length < 6 ->
                toast(R.string.change_password_too_short)
            newPass != confirm ->
                toast(R.string.change_password_mismatch)
            else -> {
                toast(R.string.change_password_success)
                findNavController().popBackStack()
            }
        }
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
