package com.urmyfood.user.presentation.auth.forgotpass

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentOtpBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * OTP verification screen fragment.
 * Allows user to enter the 6-digit OTP code sent to their email.
 * Auto-moves focus between OTP input boxes.
 */
class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by activityViewModels {
        ForgotPasswordViewModel.Factory(
            ServiceLocator.forgotPasswordUseCase,
            ServiceLocator.verifyOtpUseCase,
            ServiceLocator.resetPasswordUseCase
        )
    }

    private var countDownTimer: CountDownTimer? = null
    private lateinit var otpFields: List<EditText>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        setupOtpAutoFocus()
        setupClickListeners()
        observeViewModel()
        displayEmail()
        startResendCountdown()
    }

    private fun displayEmail() {
        binding.tvEmail.text = viewModel.email
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
            viewModel.verifyOtp(otpCode)
        }

        binding.tvResend.setOnClickListener {
            viewModel.sendOtp(viewModel.email)
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
                    findNavController().navigate(
                        R.id.action_otpFragment_to_resetPasswordFragment
                    )
                }
                is OtpUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                    clearOtpFields()
                }
            }
        }
    }

    private fun startResendCountdown() {
        binding.tvResend.isEnabled = false
        binding.tvResend.setTextColor(resources.getColor(R.color.text_hint, null))

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(RESEND_COUNTDOWN_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.tvResend.text = getString(R.string.otp_resend_countdown, seconds)
            }

            override fun onFinish() {
                binding.tvResend.text = getString(R.string.otp_resend)
                binding.tvResend.isEnabled = true
                binding.tvResend.setTextColor(resources.getColor(R.color.primary, null))
            }
        }.start()
    }

    private fun clearOtpFields() {
        otpFields.forEach { it.text.clear() }
        otpFields.firstOrNull()?.requestFocus()
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

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        private const val RESEND_COUNTDOWN_MS = 60000L
    }
}