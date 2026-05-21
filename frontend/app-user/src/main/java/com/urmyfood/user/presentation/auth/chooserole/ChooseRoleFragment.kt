package com.urmyfood.user.presentation.auth.chooserole

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthChooseRoleBinding

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.urmyfood.user.BuildConfig

class ChooseRoleFragment : Fragment() {

    private var _binding: FragmentAuthChooseRoleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChooseRoleViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideChooseRoleViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthChooseRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            com.urmyfood.user.di.ServiceLocator.tokenManager.setFirstTime(false)
            findNavController().navigate(R.id.action_chooseRoleFragment_to_loginFragment)
        }

        binding.btnRegister.setOnClickListener {
            com.urmyfood.user.di.ServiceLocator.tokenManager.setFirstTime(false)
            findNavController().navigate(R.id.action_chooseRoleFragment_to_signupCustomerFragment)
        }

        binding.btnGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }

        binding.tvGuest.setOnClickListener {
            com.urmyfood.user.di.ServiceLocator.tokenManager.setFirstTime(false)
            viewModel.loginAsGuest()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChooseRoleUiState.GuestSuccess ->
                    findNavController().navigate(R.id.action_chooseRoleFragment_to_mainContainerFragment)
                is ChooseRoleUiState.Success -> {
                    findNavController().navigate(R.id.action_chooseRoleFragment_to_mainContainerFragment)
                }
                is ChooseRoleUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun handleGoogleLogin() {
        android.util.Log.d("URMYFOOD_AUTH", ">>> [ChooseRoleFragment] Starting Google Login")
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
                    android.util.Log.d("URMYFOOD_AUTH", ">>> [ChooseRoleFragment] Received ID Token: $googleIdToken")
                    viewModel.loginWithGoogle(googleIdToken)
                } else {
                    android.util.Log.w("URMYFOOD_AUTH", ">>> [ChooseRoleFragment] Received unexpected credential type: ${credential.type}")
                }
            } catch (e: GetCredentialException) {
                android.util.Log.e("URMYFOOD_AUTH", ">>> [ChooseRoleFragment] Google Login Exception: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi đăng nhập Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
