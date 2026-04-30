package com.urmyfood.user.presentation.auth.splash

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentSplashBinding

/**
 * Splash screen fragment.
 * Displays the app branding and auto-navigates to the ChooseRole screen after a delay.
 */
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val splashHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        styleAppName()
        navigateAfterDelay()
    }

    private fun styleAppName() {
        val appName = "URMYFOOD"
        val spannable = SpannableString(appName)

        // "URMY" (0..3) → ExtraBold (weight 800)
        val extraBoldTypeface = Typeface.create(
            Typeface.create("sans-serif", Typeface.NORMAL),
            800,
            false
        )
        spannable.setSpan(
            TypefaceSpan(extraBoldTypeface),
            0, 4,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // "FOOD" (4..7) → Light (weight 300)
        val lightTypeface = Typeface.create(
            Typeface.create("sans-serif", Typeface.NORMAL),
            300,
            false
        )
        spannable.setSpan(
            TypefaceSpan(lightTypeface),
            4, 8,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvAppName.text = spannable
    }

    private fun navigateAfterDelay() {
        splashHandler.postDelayed({
            if (isAdded) {
                findNavController().navigate(
                    R.id.action_splashFragment_to_chooseRoleFragment
                )
            }
        }, SPLASH_DELAY_MS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        splashHandler.removeCallbacksAndMessages(null)
        _binding = null
    }

    companion object {
        private const val SPLASH_DELAY_MS = 2000L
    }
}