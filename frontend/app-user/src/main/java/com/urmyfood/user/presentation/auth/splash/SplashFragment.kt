package com.urmyfood.user.presentation.auth.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAuthSplashBinding
import com.urmyfood.user.util.BrandingHelper

/**
 * Splash screen fragment.
 * Displays the app branding and auto-navigates to the ChooseRole screen after a delay.
 */
class SplashFragment : Fragment() {

    private var _binding: FragmentAuthSplashBinding? = null
    private val binding get() = _binding!!

    private val splashHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BrandingHelper.styleAppName(binding.tvAppName)
        navigateAfterDelay()
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