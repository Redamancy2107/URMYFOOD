package com.urmyfood.user.presentation.auth.chooserole

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentChooseRoleBinding

/**
 * Choose Role screen fragment.
 * Allows the user to choose between Login, Register, or Google Login.
 */
class ChooseRoleFragment : Fragment() {

    private var _binding: FragmentChooseRoleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
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
            // TODO: Implement Google Sign-In
        }

        binding.tvGuest.setOnClickListener {
            // TODO: Navigate to main screen as guest
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}