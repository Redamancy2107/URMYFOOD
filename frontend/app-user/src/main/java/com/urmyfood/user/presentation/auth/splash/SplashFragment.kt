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
 * Displays the app branding and auto-navigates based on login state.
 * If logged in → MainContainerFragment (main app with bottom nav)
 * If not → ChooseRoleFragment (auth flow)
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
        
        // Splash screen always shows with delay
        navigateAfterDelay()
    }

    private fun navigateAfterDelay() {
        splashHandler.postDelayed({
            if (isAdded) {
                navigateToNextScreen()
            }
        }, SPLASH_DELAY_MS)
    }

    private fun navigateToNextScreen() {
        val tokenManager = com.urmyfood.user.di.ServiceLocator.tokenManager
        val isLoggedIn = tokenManager.isLoggedIn()
        
        if (isLoggedIn) {
            findNavController().navigate(R.id.action_splashFragment_to_mainContainerFragment)
        } else {
            if (tokenManager.isFirstTime()) {
                // First time ever: show ChooseRole (onboarding)
                findNavController().navigate(R.id.action_splashFragment_to_chooseRoleFragment)
            } else {
                // Not first time: go straight to Login
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
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