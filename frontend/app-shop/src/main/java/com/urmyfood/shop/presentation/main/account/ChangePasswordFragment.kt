package com.urmyfood.shop.presentation.main.account

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentChangePasswordBinding
import com.urmyfood.shop.di.ServiceLocator

/**
 * Màn hình Đổi mật khẩu cho app-shop.
 */
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChangePasswordViewModel by viewModels {
        ServiceLocator.provideChangePasswordViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // Toggle visibility for each password field
        setupToggle(binding.etCurrentPassword, binding.btnToggleCurrent)
        setupToggle(binding.etNewPassword, binding.btnToggleNew)
        setupToggle(binding.etConfirmPassword, binding.btnToggleConfirm)

        binding.tvForgotPassword.setOnClickListener {
            try {
                val parentNavController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                parentNavController.navigate(R.id.forgotPasswordFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnConfirm.setOnClickListener { validateAndSubmit() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChangePasswordUiState.Loading -> {
                    binding.btnConfirm.isEnabled = false
                    binding.btnConfirm.text = "Đang xử lý..."
                }
                is ChangePasswordUiState.Success -> {
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = getString(R.string.change_password_btn)
                    toast(R.string.change_password_success)
                    findNavController().popBackStack()
                }
                is ChangePasswordUiState.Error -> {
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = getString(R.string.change_password_btn)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ChangePasswordUiState.Idle -> Unit
            }
        }
    }

    private fun setupToggle(editText: EditText, toggleBtn: ImageButton) {
        toggleBtn.setOnClickListener {
            val isHidden = editText.transformationMethod is PasswordTransformationMethod
            editText.transformationMethod = if (isHidden) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            toggleBtn.setImageResource(if (isHidden) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
            editText.setSelection(editText.text.length)
        }
    }

    private fun validateAndSubmit() {
        val current = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        when {
            current.isEmpty() || newPass.isEmpty() || confirm.isEmpty() ->
                toast(R.string.change_password_empty)
            newPass.length < 6 ->
                toast(R.string.change_password_too_short)
            newPass != confirm ->
                toast(R.string.change_password_mismatch)
            else -> {
                viewModel.changePassword(current, newPass)
            }
        }
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
