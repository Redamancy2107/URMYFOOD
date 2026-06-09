package com.urmyfood.admin.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.annotation.SuppressLint
import android.text.InputType
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.urmyfood.admin.R
import com.urmyfood.admin.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnContinue.setOnClickListener {
            // Validate and navigate to OTP
            findNavController().navigate(R.id.action_loginFragment_to_otpFragment)
        }
        
        setupPasswordToggle()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle() {
        var isPasswordVisible = false
        binding.etPassword.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val rightDrawable = binding.etPassword.compoundDrawables[DRAWABLE_RIGHT]
                if (rightDrawable != null && event.x >= (binding.etPassword.width - binding.etPassword.compoundPaddingRight)) {
                    isPasswordVisible = !isPasswordVisible
                    
                    // Preserve typeface when changing input type
                    val currentTypeface = binding.etPassword.typeface
                    
                    if (isPasswordVisible) {
                        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.etPassword.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock_outline),
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_eye_off_outline),
                            null
                        )
                    } else {
                        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.etPassword.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock_outline),
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_eye_outline),
                            null
                        )
                    }
                    
                    binding.etPassword.typeface = currentTypeface
                    // move cursor to end
                    binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
