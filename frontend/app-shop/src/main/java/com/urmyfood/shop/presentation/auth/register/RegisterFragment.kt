package com.urmyfood.shop.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentAuthRegisterBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.common.*

class RegisterFragment : Fragment() {

    private var _binding: FragmentAuthRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideRegisterViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.tvTermsLink.setOnClickListener {
            TermsBottomSheetFragment.newInstance()
                .show(parentFragmentManager, TermsBottomSheetFragment.TAG)
        }
        binding.tvLoginLink.setOnClickListener {
            findNavController().safeNavigate(R.id.action_register_to_login)
        }
        binding.btnRegister.setOnClickListener {
            viewModel.submitForm(
                fullName = binding.etName.text.toString(),
                email = binding.etEmail.text.toString(),
                phone = binding.etPhone.text.toString(),
                password = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString(),
                termsAccepted = binding.cbTerms.isChecked
            )
        }
    }

    private fun observeViewModel() {
        viewModel.formUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.FormUiState.Idle -> {
                    binding.btnRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is RegisterViewModel.FormUiState.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is RegisterViewModel.FormUiState.OtpSent -> {
                    binding.btnRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    viewModel.resetFormState()
                    findNavController().safeNavigate(
                        R.id.action_register_to_otp,
                        Bundle().apply { putString("otpSource", "registration") }
                    )
                }
                is RegisterViewModel.FormUiState.Error -> {
                    binding.btnRegister.isEnabled = true
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
