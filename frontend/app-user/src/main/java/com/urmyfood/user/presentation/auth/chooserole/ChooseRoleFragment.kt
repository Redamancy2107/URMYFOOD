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
            showFeatureInDevelopment()
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
                else -> Unit
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
