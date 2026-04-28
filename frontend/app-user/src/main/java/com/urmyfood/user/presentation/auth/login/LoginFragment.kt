package com.urmyfood.user.presentation.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentLoginBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Login screen fragment.
 * Handles user login with email/phone and password.
 * Uses LoginViewModel for business logic and state management.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(ServiceLocator.loginUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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

        binding.btnLogin.setOnClickListener {
            val emailOrPhone = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(emailOrPhone, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(
                R.id.action_loginFragment_to_forgotPasswordFragment
            )
        }

        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigateUp()
            findNavController().navigate(
                R.id.action_chooseRoleFragment_to_signupCustomerFragment
            )
        }

        binding.btnGoogleLogin.setOnClickListener {
            // TODO: Implement Google Sign-In
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginUiState.Idle -> {
                    setLoading(false)
                }
                is LoginUiState.Loading -> {
                    setLoading(true)
                    hideError()
                }
                is LoginUiState.Success -> {
                    setLoading(false)
                    // TODO: Save token and navigate to main screen
                }
                is LoginUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else getString(R.string.login_btn)
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