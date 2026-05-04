package com.urmyfood.user.presentation.auth.chooserole

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.urmyfood.user.BuildConfig
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthChooseRoleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Choose Role screen fragment.
 * Allows the user to choose between Login, Register, or Google Login.
 */
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
            findNavController().navigate(
                R.id.action_chooseRoleFragment_to_loginFragment
            )
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_chooseRoleFragment_to_signupCustomerFragment
            )
        }

        binding.btnGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }

        binding.tvGuest.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChooseRoleUiState.Idle -> {
                    // Do nothing
                }
                is ChooseRoleUiState.Loading -> {
                    // Show loading if needed
                }
                is ChooseRoleUiState.Success -> {
                    // Save token to SharedPreferences
                    com.urmyfood.user.di.ServiceLocator.tokenManager.saveToken(
                        token = state.authToken.accessToken,
                        refreshToken = state.authToken.refreshToken,
                        fullName = state.authToken.fullName,
                        role = state.authToken.role
                    )

                    Toast.makeText(
                        requireContext(),
                        "Đăng nhập Google thành công! Chào mừng ${state.authToken.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to main screen
                    findNavController().popBackStack(R.id.chooseRoleFragment, true)
                }
                is ChooseRoleUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
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

        CoroutineScope(Dispatchers.Main).launch {
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