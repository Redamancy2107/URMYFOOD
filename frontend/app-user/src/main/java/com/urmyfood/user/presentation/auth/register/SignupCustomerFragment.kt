package com.urmyfood.user.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentSignupCustomerBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Registration screen fragment.
 * Handles new customer account creation.
 * Uses RegisterViewModel for business logic and state management.
 */
class SignupCustomerFragment : Fragment() {

    private var _binding: FragmentSignupCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.Factory(ServiceLocator.registerUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupCustomerBinding.inflate(inflater, container, false)
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
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            viewModel.register(name, email, phone, password, confirmPassword)
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigateUp()
            findNavController().navigate(
                R.id.action_chooseRoleFragment_to_loginFragment
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
                is RegisterUiState.Success -> {
                    setLoading(false)
                    Snackbar.make(
                        binding.root,
                        "Đăng ký thành công! Vui lòng đăng nhập.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    // Navigate back to login
                    findNavController().navigateUp()
                    findNavController().navigate(
                        R.id.action_chooseRoleFragment_to_loginFragment
                    )
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