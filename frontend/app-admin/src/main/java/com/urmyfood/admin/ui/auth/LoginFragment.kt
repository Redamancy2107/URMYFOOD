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
import androidx.lifecycle.lifecycleScope
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.OtpRequest
import com.urmyfood.admin.data.network.RetrofitClient
import com.urmyfood.admin.databinding.FragmentLoginBinding
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

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
            handleLogin()
        }
        
        setupPasswordToggle()
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            showError("Vui lòng nhập email")
            return
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu")
            return
        }

        hideError()
        setLoadingState(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // For admin OTP flow, we trigger the OTP sending using email
                val response = RetrofitClient.api.sendAdminOtp(OtpRequest(email = email))
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        // Navigate to OTP entry screen, passing email to verify
                        val bundle = Bundle().apply {
                            putString("email", email)
                        }
                        findNavController().navigate(R.id.action_loginFragment_to_otpFragment, bundle)
                    } else {
                        showError(apiResponse?.message ?: "Đã xảy ra lỗi không xác định")
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    showError(errorMsg)
                }
            } catch (e: Exception) {
                showError("Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại kết nối mạng.")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.btnContinue.isEnabled = false
            binding.btnContinue.text = "Đang xử lý..."
        } else {
            binding.btnContinue.isEnabled = true
            binding.btnContinue.text = "Đăng nhập"
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            json.get("message")?.asString ?: "Đăng nhập thất bại"
        } catch (e: Exception) {
            "Lỗi kết nối máy chủ"
        }
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
