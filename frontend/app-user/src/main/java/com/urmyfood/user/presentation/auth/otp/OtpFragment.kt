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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthOtpBinding

/**
 * OTP verification screen fragment.
 * Allows user to enter the 6-digit OTP code sent to their email.
 * Auto-moves focus between OTP input boxes.
 *
 * Supports two flows via the "otpSource" argument:
 * - "register": After OTP submit → navigate to login screen
 * - "forgot_password": After OTP submit → navigate to reset password screen
 */
class OtpFragment : Fragment() {

    private var _binding: FragmentAuthOtpBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private lateinit var otpFields: List<EditText>

    /** Source of the OTP flow: "register" or "forgot_password" */
    private val otpSource: String by lazy {
        arguments?.getString("otpSource") ?: "forgot_password"
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

        setupOtpAutoFocus()
        setupClickListeners()
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
            // Frontend-only: navigate based on source
            when (otpSource) {
                "register" -> {
                    // After register OTP → go back to login screen
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.otp_verify_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(
                        R.id.action_otpFragment_to_loginFragment
                    )
                }
                "forgot_password" -> {
                    // After forgot password OTP → go to reset password screen
                    findNavController().navigate(
                        R.id.action_otpFragment_to_resetPasswordFragment
                    )
                }
            }
        }

        binding.tvResend.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
            startResendCountdown()
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

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        private const val RESEND_COUNTDOWN_MS = 60000L
    }
}
