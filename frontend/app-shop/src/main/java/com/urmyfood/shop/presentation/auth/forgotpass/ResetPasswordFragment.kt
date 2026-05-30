package com.urmyfood.shop.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentAuthResetPasswordBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.common.safeNavigate

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentAuthResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideForgotPasswordViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnResetPassword.setOnClickListener {
            viewModel.resetPassword(
                newPassword = binding.etNewPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.resetUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordViewModel.ResetUiState.Idle -> {
                    binding.btnResetPassword.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is ForgotPasswordViewModel.ResetUiState.Loading -> {
                    binding.btnResetPassword.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is ForgotPasswordViewModel.ResetUiState.Success -> {
                    viewModel.resetResetState()
                    findNavController().safeNavigate(R.id.action_resetPassword_to_login)
                }
                is ForgotPasswordViewModel.ResetUiState.Error -> {
                    binding.btnResetPassword.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
