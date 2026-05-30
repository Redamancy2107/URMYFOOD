package com.urmyfood.shop.presentation.auth.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.di.ServiceLocator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_auth_splash, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            val dest = if (ServiceLocator.tokenManager.isLoggedIn()) {
                R.id.action_splash_to_main
            } else {
                R.id.action_splash_to_landing
            }
            findNavController().let { nav ->
                if (nav.currentDestination?.id == R.id.splashFragment) {
                    nav.navigate(dest)
                }
            }
        }
    }
}
