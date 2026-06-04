package com.urmyfood.shop.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentAuthForgotPasswordBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.common.safeNavigate

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentAuthForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideForgotPasswordViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSendOtp.setOnClickListener {
            viewModel.sendForgotPassword(binding.etEmail.text.toString())
        }
    }

    private fun observeViewModel() {
        viewModel.forgotUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordViewModel.ForgotUiState.Idle -> {
                    binding.btnSendOtp.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is ForgotPasswordViewModel.ForgotUiState.Loading -> {
                    binding.btnSendOtp.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is ForgotPasswordViewModel.ForgotUiState.Success -> {
                    viewModel.resetForgotState()
                    findNavController().safeNavigate(R.id.action_forgotPassword_to_otp)
                }
                is ForgotPasswordViewModel.ForgotUiState.Error -> {
                    binding.btnSendOtp.isEnabled = true
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
