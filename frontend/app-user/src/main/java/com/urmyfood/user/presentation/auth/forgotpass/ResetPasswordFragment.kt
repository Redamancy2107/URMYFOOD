package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthResetPasswordBinding
import androidx.fragment.app.activityViewModels
import com.urmyfood.user.di.ServiceLocator

/**
 * Reset Password screen fragment.
 * Allows user to set a new password after OTP verification.
 * Currently frontend-only: navigates back to login on submit.
 */
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentAuthResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideForgotPasswordViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (newPassword.isEmpty()) {
                showError("Vui lòng nhập mật khẩu mới")
                return@setOnClickListener
            }
            if (newPassword.length < 6) {
                showError("Mật khẩu phải có ít nhất 6 ký tự")
                return@setOnClickListener
            }
            if (newPassword != confirmPassword) {
                showError("Mật khẩu xác nhận không khớp")
                return@setOnClickListener
            }

            // viewModel.resetPassword(newPassword, confirmPassword) // Tạm thời bỏ qua check BE
            Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.resetPasswordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResetPasswordUiState.Idle -> {
                    setLoading(false)
                }
                is ResetPasswordUiState.Loading -> {
                    setLoading(true)
                    hideError()
                }
                is ResetPasswordUiState.Success -> {
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.reset_password_success),
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(
                        R.id.action_resetPasswordFragment_to_loginFragment
                    )
                }
                is ResetPasswordUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnResetPassword.isEnabled = !isLoading
        binding.btnResetPassword.text = if (isLoading) "" else getString(R.string.reset_password_btn)
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.isVisible = true
    }

    private fun hideError() {
        binding.tvError.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}