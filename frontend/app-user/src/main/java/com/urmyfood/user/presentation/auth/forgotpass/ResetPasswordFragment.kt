package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentResetPasswordBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Reset Password screen fragment.
 * Allows user to set a new password after OTP verification.
 * Uses shared ForgotPasswordViewModel scoped to the activity.
 */
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by activityViewModels {
        ForgotPasswordViewModel.Factory(
            ServiceLocator.forgotPasswordUseCase,
            ServiceLocator.verifyOtpUseCase,
            ServiceLocator.resetPasswordUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
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
            viewModel.resetPassword(newPassword, confirmPassword)
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
                    Snackbar.make(
                        binding.root,
                        getString(R.string.reset_password_success),
                        Snackbar.LENGTH_LONG
                    ).show()
                    // Navigate back to login screen
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
        binding.btnResetPassword.text =
            if (isLoading) "" else getString(R.string.reset_password_btn)
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