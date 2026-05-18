package com.urmyfood.user.presentation.auth.login

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
import com.urmyfood.user.databinding.FragmentAuthLoginBinding
import com.urmyfood.user.BuildConfig
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideLoginViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthLoginBinding.inflate(inflater, container, false)
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
            findNavController().navigate(
                R.id.action_loginFragment_to_signupCustomerFragment
            )
        }

        binding.btnGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }

        binding.btnLoginOtp.setOnClickListener {
            showFeatureInDevelopment()
        }

        binding.tvGuest.setOnClickListener {
            viewModel.loginAsGuest()
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
                    findNavController().navigate(
                        R.id.action_loginFragment_to_mainContainerFragment
                    )
                }
                is LoginUiState.GuestSuccess -> {
                    setLoading(false)
                    findNavController().navigate(R.id.action_loginFragment_to_mainContainerFragment)
                }
                is LoginUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun handleGoogleLogin() {
        android.util.Log.d("URMYFOOD_AUTH", ">>> [LoginFragment] Starting Google Login")
        val credentialManager = CredentialManager.create(requireContext())
        
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext(),
                )
                
                val credential = result.credential
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken
                    android.util.Log.d("URMYFOOD_AUTH", ">>> [LoginFragment] Received ID Token: $googleIdToken")
                    viewModel.loginWithGoogle(googleIdToken)
                } else {
                    android.util.Log.w("URMYFOOD_AUTH", ">>> [LoginFragment] Received unexpected credential type: ${credential.type}")
                }
            } catch (e: GetCredentialException) {
                android.util.Log.e("URMYFOOD_AUTH", ">>> [LoginFragment] Google Login Exception: ${e.message}", e)
                showError("Lỗi đăng nhập Google: ${e.message}")
            }
        }
    }

    private fun showFeatureInDevelopment() {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
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