package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentForgotPasswordBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Forgot Password screen fragment.
 * Allows user to enter email to receive a password reset OTP.
 * Uses shared ForgotPasswordViewModel scoped to the activity.
 */
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
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
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
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

        binding.btnSendOtp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.sendOtp(email)
        }
    }

    private fun observeViewModel() {
        viewModel.forgotPasswordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordUiState.Idle -> {
                    setLoading(false)
                }
                is ForgotPasswordUiState.Loading -> {
                    setLoading(true)
                    hideError()
                }
                is ForgotPasswordUiState.Success -> {
                    setLoading(false)
                    findNavController().navigate(
                        R.id.action_forgotPasswordFragment_to_otpFragment
                    )
                }
                is ForgotPasswordUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnSendOtp.isEnabled = !isLoading
        binding.btnSendOtp.text = if (isLoading) "" else getString(R.string.forgot_password_btn)
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