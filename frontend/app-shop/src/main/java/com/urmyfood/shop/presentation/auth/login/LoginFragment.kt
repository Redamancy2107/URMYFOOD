package com.urmyfood.shop.presentation.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentAuthLoginBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.common.safeNavigate

class LoginFragment : Fragment() {

    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        ServiceLocator.provideLoginViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        setFragmentResultListener(WrongRoleBottomSheetFragment.KEY) { _, _ ->
            findNavController().safeNavigate(R.id.action_login_to_register)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            viewModel.login(
                emailOrPhone = binding.etEmailOrPhone.text.toString(),
                password = binding.etPassword.text.toString()
            )
        }
        binding.tvRegister.setOnClickListener {
            findNavController().safeNavigate(R.id.action_login_to_register)
        }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().safeNavigate(R.id.action_login_to_forgotPassword)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.UiState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is LoginViewModel.UiState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is LoginViewModel.UiState.Success -> {
                    viewModel.resetState()
                    findNavController().safeNavigate(R.id.action_login_to_main)
                }
                is LoginViewModel.UiState.WrongRole -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    viewModel.resetState()
                    WrongRoleBottomSheetFragment.newInstance()
                        .show(parentFragmentManager, WrongRoleBottomSheetFragment.TAG)
                }
                is LoginViewModel.UiState.Error -> {
                    binding.btnLogin.isEnabled = true
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
