package com.urmyfood.admin.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.urmyfood.admin.R
import com.urmyfood.admin.data.local.SessionManager
import com.urmyfood.admin.data.model.OtpLoginRequest
import com.urmyfood.admin.data.network.RetrofitClient
import com.urmyfood.admin.databinding.FragmentOtpBinding
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {
    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private var email: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        email = arguments?.getString("email") ?: ""
        
        setupOtpInputFields()
        
        binding.btnConfirm.setOnClickListener {
            verifyOtp()
        }
    }

    private fun setupOtpInputFields() {
        val editTexts = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        for (i in editTexts.indices) {
            val currentEditText = editTexts[i]
            
            // Text change listener for auto-forwarding focus
            currentEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s != null && s.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    }
                }
            })

            // Key listener for backspace key to delete and go back
            currentEditText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (currentEditText.text.isEmpty() && i > 0) {
                        editTexts[i - 1].requestFocus()
                        editTexts[i - 1].text.clear()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun verifyOtp() {
        val editTexts = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        val codeBuilder = StringBuilder()
        for (et in editTexts) {
            codeBuilder.append(et.text.toString().trim())
        }
        val otpCode = codeBuilder.toString()

        if (otpCode.length < 6) {
            showError("Vui lòng nhập đầy đủ 6 chữ số OTP")
            return
        }

        hideError()
        setLoadingState(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.loginAdminOtp(
                    OtpLoginRequest(email = email, code = otpCode)
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        
                        // Save session with real data from server
                        SessionManager.saveSession(
                            token = authData.token,
                            refreshToken = authData.refreshToken,
                            accountId = authData.accountId ?: -1L,
                            fullName = authData.fullName,
                            role = authData.role,
                            email = email
                        )
                        
                        // Navigate to Dashboard upon successful login
                        findNavController().navigate(R.id.action_otpFragment_to_dashboardFragment)
                    } else {
                        showError(apiResponse?.message ?: "Xác nhận OTP thất bại")
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    showError(errorMsg)
                }
            } catch (e: Exception) {
                showError("Lỗi kết nối đến máy chủ. Vui lòng thử lại.")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun showError(message: String) {
        binding.tvErrorOtp.text = message
        binding.tvErrorOtp.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvErrorOtp.visibility = View.GONE
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.text = "Đang xác thực..."
        } else {
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.text = "Xác nhận"
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            json.get("message")?.asString ?: "Xác nhận OTP thất bại"
        } catch (e: Exception) {
            "Lỗi kết nối máy chủ"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
