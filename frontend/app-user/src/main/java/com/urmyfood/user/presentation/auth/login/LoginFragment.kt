package com.urmyfood.user.presentation.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthLoginBinding

/**
 * Login screen fragment.
 * Handles user login with email/phone and password.
 * Currently frontend-only: login, Google and Guest show "feature in development" toast.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnLogin.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
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
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.tvGuest.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}