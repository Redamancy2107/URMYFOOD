package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthForgotPasswordBinding

/**
 * Forgot Password screen fragment.
 * Allows user to enter email to receive a password reset OTP.
 * Currently frontend-only: navigates directly to OTP screen.
 */
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentAuthForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthForgotPasswordBinding.inflate(inflater, container, false)
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

        binding.btnSendOtp.setOnClickListener {
            // Frontend-only: navigate directly to OTP with source = "forgot_password"
            findNavController().navigate(
                R.id.action_forgotPasswordFragment_to_otpFragment,
                bundleOf("otpSource" to "forgot_password")
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}