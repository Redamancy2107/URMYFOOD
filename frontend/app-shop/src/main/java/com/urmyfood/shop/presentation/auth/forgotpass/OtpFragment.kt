package com.urmyfood.shop.presentation.auth.forgotpass

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentAuthOtpBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.auth.register.RegisterViewModel
import com.urmyfood.shop.presentation.common.safeNavigate

class OtpFragment : Fragment() {

    private var _binding: FragmentAuthOtpBinding? = null
    private val binding get() = _binding!!

    private val forgotViewModel: ForgotPasswordViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideForgotPasswordViewModelFactory()
    }

    private val registerViewModel: RegisterViewModel by navGraphViewModels(R.id.nav_graph_auth) {
        ServiceLocator.provideRegisterViewModelFactory()
    }

    private val otpSource: String by lazy {
        arguments?.getString("otpSource") ?: SOURCE_FORGOT_PASSWORD
    }

    private var countDownTimer: CountDownTimer? = null
    private lateinit var otpFields: List<EditText>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        binding.tvEmail.text = if (otpSource == SOURCE_REGISTRATION) {
            registerViewModel.email
        } else {
            forgotViewModel.email
        }
        setupOtpInputs()
        setupClickListeners()
        observeViewModel()
        startResendCountdown()
    }

    private fun setupOtpInputs() {
        otpFields.forEachIndexed { index, field ->
            field.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.lastIndex) {
                        otpFields[index + 1].requestFocus()
                    }
                }
            })
            field.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN &&
                    field.text.isEmpty() && index > 0
                ) {
                    otpFields[index - 1].apply {
                        setText("")
                        requestFocus()
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun getOtpCode(): String = otpFields.joinToString("") { it.text.toString() }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnVerifyOtp.setOnClickListener {
            val code = getOtpCode()
            if (otpSource == SOURCE_REGISTRATION) {
                registerViewModel.register(code)
            } else {
                forgotViewModel.verifyOtp(code)
            }
        }
        binding.tvResend.setOnClickListener {
            if (otpSource == SOURCE_REGISTRATION) registerViewModel.resendOtp()
            else forgotViewModel.resendForgotPassword()
            startResendCountdown()
        }
    }

    private fun observeViewModel() {
        if (otpSource == SOURCE_REGISTRATION) observeRegister() else observeForgot()
    }

    private fun observeForgot() {
        forgotViewModel.otpUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordViewModel.OtpUiState.Idle -> setLoading(false)
                is ForgotPasswordViewModel.OtpUiState.Loading -> setLoading(true)
                is ForgotPasswordViewModel.OtpUiState.Success -> {
                    setLoading(false)
                    forgotViewModel.resetOtpState()
                    findNavController().safeNavigate(R.id.action_otp_to_resetPassword)
                }
                is ForgotPasswordViewModel.OtpUiState.Error -> showError(state.message)
            }
        }
    }

    private fun observeRegister() {
        registerViewModel.registerUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegisterUiState.Idle -> setLoading(false)
                is RegisterViewModel.RegisterUiState.Loading -> setLoading(true)
                is RegisterViewModel.RegisterUiState.Success -> {
                    setLoading(false)
                    registerViewModel.resetRegisterState()
                    findNavController().safeNavigate(R.id.action_otp_to_registration_flow)
                }
                is RegisterViewModel.RegisterUiState.Error -> showError(state.message)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnVerifyOtp.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) binding.tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        setLoading(false)
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        otpFields.forEach { it.text.clear() }
        otpFields.firstOrNull()?.requestFocus()
    }

    private fun startResendCountdown() {
        binding.tvResend.isEnabled = false
        binding.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(RESEND_COUNTDOWN_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                _binding?.tvResend?.text = getString(R.string.otp_resend_countdown, seconds)
            }

            override fun onFinish() {
                _binding?.tvResend?.apply {
                    text = getString(R.string.otp_resend)
                    isEnabled = true
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RESEND_COUNTDOWN_MS = 60000L
        private const val SOURCE_REGISTRATION = "registration"
        private const val SOURCE_FORGOT_PASSWORD = "forgot_password"
    }
}
