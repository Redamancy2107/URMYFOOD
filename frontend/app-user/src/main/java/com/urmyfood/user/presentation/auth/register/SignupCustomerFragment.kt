package com.urmyfood.user.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthSignupCustomerBinding

/**
 * Registration screen fragment.
 * Handles new customer account creation.
 * Currently frontend-only: register button navigates directly to OTP screen.
 */
class SignupCustomerFragment : Fragment() {

    private var _binding: FragmentAuthSignupCustomerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthSignupCustomerBinding.inflate(inflater, container, false)
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

        binding.btnRegister.setOnClickListener {
            // Frontend-only: navigate directly to OTP with source = "register"
            findNavController().navigate(
                R.id.action_signupCustomerFragment_to_otpFragment,
                bundleOf("otpSource" to "register")
            )
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigateUp()
            findNavController().navigate(
                R.id.action_chooseRoleFragment_to_loginFragment
            )
        }

        binding.tvTermsLink.setOnClickListener {
            val dialog = TermsDialogFragment()
            dialog.setOnAgreeClickListener {
                binding.cbTerms.isChecked = true
            }
            dialog.show(childFragmentManager, TermsDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}