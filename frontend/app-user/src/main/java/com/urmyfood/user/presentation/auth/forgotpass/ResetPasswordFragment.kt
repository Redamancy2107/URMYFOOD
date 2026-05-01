package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthResetPasswordBinding

/**
 * Reset Password screen fragment.
 * Allows user to set a new password after OTP verification.
 * Currently frontend-only: navigates back to login on submit.
 */
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentAuthResetPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthResetPasswordBinding.inflate(inflater, container, false)
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

        binding.btnResetPassword.setOnClickListener {
            // Frontend-only: show success toast and navigate to login
            Toast.makeText(
                requireContext(),
                getString(R.string.reset_password_success),
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigate(
                R.id.action_resetPasswordFragment_to_loginFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}