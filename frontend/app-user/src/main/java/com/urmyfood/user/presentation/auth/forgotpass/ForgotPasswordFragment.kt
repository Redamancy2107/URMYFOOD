package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthForgotPasswordBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Forgot Password screen fragment.
 * Allows user to enter email to receive a password reset OTP.
 */
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentAuthForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideForgotPasswordViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthForgotPasswordBinding.inflate(inflater, container, false)
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
            if (email.isEmpty()) {
                showError("Vui lòng nhập email")
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Email không hợp lệ")
                return@setOnClickListener
            }
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
                        R.id.action_forgotPasswordFragment_to_otpFragment,
                        bundleOf(
                            "otpSource" to "forgot_password",
                            "email" to binding.etEmail.text.toString().trim()
                        )
                    )
                }
                is ForgotPasswordUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                    android.widget.Toast.makeText(requireContext(), state.message, android.widget.Toast.LENGTH_SHORT).show()
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