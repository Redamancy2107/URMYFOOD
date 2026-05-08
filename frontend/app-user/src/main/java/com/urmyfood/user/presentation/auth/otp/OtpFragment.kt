package com.urmyfood.user.presentation.auth.otp

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthOtpBinding
import com.urmyfood.user.presentation.auth.forgotpass.ForgotPasswordUiState
import com.urmyfood.user.presentation.auth.forgotpass.ForgotPasswordViewModel
import com.urmyfood.user.presentation.auth.forgotpass.OtpUiState
import kotlin.getValue
import com.urmyfood.user.di.ServiceLocator

/**
 * OTP verification screen fragment.
 * Allows user to enter the 6-digit OTP code sent to their email.
 * Auto-moves focus between OTP input boxes.
 */
class OtpFragment : Fragment() {

    private var _binding: FragmentAuthOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideForgotPasswordViewModelFactory()
    }

    private var countDownTimer: CountDownTimer? = null
    private lateinit var otpFields: List<EditText>

    /** Source of the OTP flow: "register" or "forgot_password" */
    private val otpSource: String by lazy {
        arguments?.getString("otpSource") ?: "forgot_password"
    }

    private val email: String by lazy {
        arguments?.getString("email") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        if (email.isNotEmpty()) {
            viewModel.setEmail(email)
        }
        binding.tvEmail.text = viewModel.email

        setupOtpAutoFocus()
        setupClickListeners()
        observeViewModel()
        startResendCountdown()
    }

    private fun setupOtpAutoFocus() {
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL
                    && event.action == android.view.KeyEvent.ACTION_DOWN
                    && editText.text.isEmpty()
                    && index > 0
                ) {
                    otpFields[index - 1].requestFocus()
                    otpFields[index - 1].text.clear()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnVerifyOtp.setOnClickListener {
            val otpCode = otpFields.joinToString("") { it.text.toString() }
            if (otpCode.length < 6) {
                showError("Vui lòng nhập đủ 6 chữ số")
                return@setOnClickListener
            }
            viewModel.verifyOtp(otpCode)
        }

        binding.tvResend.setOnClickListener {
            val targetEmail = if (email.isNotEmpty()) email else viewModel.email
            viewModel.sendOtp(targetEmail)
            startResendCountdown()
        }
    }

    private fun observeViewModel() {
        viewModel.otpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OtpUiState.Idle -> {
                    setLoading(false)
                }
                is OtpUiState.Loading -> {
                    setLoading(true)
                    hideError()
                }
                is OtpUiState.Success -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), getString(R.string.otp_verify_success), Toast.LENGTH_SHORT).show()
                    
                    if (otpSource == "register") {
                        findNavController().navigate(
                            R.id.action_otpFragment_to_loginFragment
                        )
                    } else {
                        findNavController().navigate(
                            R.id.action_otpFragment_to_resetPasswordFragment
                        )
                    }
                }
                is OtpUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }

        // Also observe forgotPasswordState for Resend OTP action
        viewModel.forgotPasswordState.observe(viewLifecycleOwner) { state ->
            if (state is ForgotPasswordUiState.Success) {
                Toast.makeText(requireContext(), "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show()
            } else if (state is ForgotPasswordUiState.Error) {
                showError(state.message)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnVerifyOtp.isEnabled = !isLoading
        binding.btnVerifyOtp.text = if (isLoading) "" else getString(R.string.otp_btn)
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.isVisible = true
    }

    private fun hideError() {
        binding.tvError.isVisible = false
    }

    private fun startResendCountdown() {
        binding.tvResend.isEnabled = false
        binding.tvResend.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_hint))

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(RESEND_COUNTDOWN_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.tvResend.text = getString(R.string.otp_resend_countdown, seconds)
            }

            override fun onFinish() {
                binding.tvResend.text = getString(R.string.otp_resend)
                binding.tvResend.isEnabled = true
                binding.tvResend.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary))
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        private const val RESEND_COUNTDOWN_MS = 60000L
    }
}
