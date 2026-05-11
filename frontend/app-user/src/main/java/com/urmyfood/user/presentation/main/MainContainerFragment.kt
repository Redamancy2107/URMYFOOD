package com.urmyfood.user.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainContainerBinding

/**
 * Main container fragment that hosts the bottom navigation bar
 * and the inner NavHostFragment for the 4 main tabs.
 */
class MainContainerFragment : Fragment() {

    private var _binding: FragmentMainContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = setupBottomNavigation()
        setupBackNavigation(navController)
    }

    private fun setupBottomNavigation(): NavController {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_main) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
        return navController
    }

    private fun setupBackNavigation(navController: NavController) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (navController.currentDestination?.id != R.id.homeFragment) {
                binding.bottomNavigation.selectedItemId = R.id.homeFragment
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
