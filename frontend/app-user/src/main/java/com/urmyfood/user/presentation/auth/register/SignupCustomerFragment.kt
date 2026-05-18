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
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthSignupCustomerBinding

/**
 * Registration screen fragment.
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

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                showError("Vui lòng điền đầy đủ thông tin")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showError("Mật khẩu xác nhận không khớp")
                return@setOnClickListener
            }

            if (!binding.cbTerms.isChecked) {
                showError("Bạn phải đồng ý với Điều khoản sử dụng")
                return@setOnClickListener
            }

            setLoading(true)
            viewModel.sendOtp(email, phone)
        }



        binding.tvTermsLink.setOnClickListener {
            val termsDialog = TermsDialogFragment()
            termsDialog.setOnAgreeClickListener {
                binding.cbTerms.isChecked = true
            }
            termsDialog.show(parentFragmentManager, TermsDialogFragment.TAG)
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(
                R.id.action_signupCustomerFragment_to_loginFragment
            )
        }
    }

    private fun observeViewModel() {
        viewModel.sendOtpState.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            
            when (result) {
                is com.urmyfood.user.domain.model.Result.Success -> {
                    setLoading(false)
                    val bundle = Bundle().apply {
                        putString("otpSource", "registration")
                        putString("fullName", binding.etName.text.toString().trim())
                        putString("email", binding.etEmail.text.toString().trim())
                        putString("phone", binding.etPhone.text.toString().trim())
                        putString("password", binding.etPassword.text.toString())
                    }
                    findNavController().navigate(R.id.action_signupCustomerFragment_to_otpFragment, bundle)
                }
                is com.urmyfood.user.domain.model.Result.Error -> {
                    setLoading(false)
                    showError(result.message)
                }
            }
        }

        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterUiState.Idle -> {
                    setLoading(false)
                }
                is RegisterUiState.Loading -> {
                    setLoading(true)
                    hideError()
                }

                is RegisterUiState.Success -> {
                    setLoading(false)
                    // Navigation or success state handling
                }
                is RegisterUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
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