package com.urmyfood.user.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthSignupCustomerBinding

/**
 * Registration screen fragment.
 * Handles new customer account creation.
 * Uses RegisterViewModel for business logic and state management.
 */
class SignupCustomerFragment : Fragment() {

    private var _binding: FragmentAuthSignupCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideRegisterViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthSignupCustomerBinding.inflate(inflater, container, false)
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

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val otpCode = binding.etOtp.text.toString().trim()

            if (!binding.cbTerms.isChecked) {
                showError("Bạn phải đồng ý với Điều khoản sử dụng")
                return@setOnClickListener
            }

            viewModel.register(fullName, email, phone, password, confirmPassword, otpCode)
        }

        binding.btnSendOtp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isBlank()) {
                showError("Vui lòng nhập email trước khi yêu cầu OTP")
                return@setOnClickListener
            }
            viewModel.sendOtp(email)
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(
                R.id.action_signupCustomerFragment_to_loginFragment
            )
        }

        binding.tvTermsLink.setOnClickListener {
            val dialog = TermsDialogFragment()
            dialog.setOnAgreeClickListener {
                binding.cbTerms.isChecked = true
            }
            dialog.show(childFragmentManager, TermsDialogFragment.TAG)
        }
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterUiState.Idle -> {
                    setLoading(false)
                }
                is RegisterUiState.Loading -> {
                    setLoading(true)
                    hideError()
                }
                is RegisterUiState.OtpSent -> {
                    setLoading(false)
                    binding.tilOtp.isVisible = true
                    binding.btnSendOtp.text = "Gửi lại mã OTP"
                    Toast.makeText(requireContext(), "Mã OTP đã được gửi về email của bạn", Toast.LENGTH_SHORT).show()
                }
                is RegisterUiState.Success -> {
                    setLoading(false)
                    // Save token to SharedPreferences
                    com.urmyfood.user.di.ServiceLocator.tokenManager.saveToken(
                        token = state.authToken.accessToken,
                        refreshToken = state.authToken.refreshToken,
                        fullName = state.authToken.fullName,
                        role = state.authToken.role
                    )

                    Toast.makeText(
                        requireContext(),
                        "Đăng ký thành công! Chào mừng ${state.authToken.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // TODO: Navigate to main screen
                    findNavController().popBackStack(R.id.chooseRoleFragment, true)
                }
                is RegisterUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "" else getString(R.string.register_btn)
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