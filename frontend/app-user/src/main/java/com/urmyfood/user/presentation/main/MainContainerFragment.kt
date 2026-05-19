package com.urmyfood.user.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainContainerBinding

/**
 * Main container fragment that hosts the bottom navigation bar
 * and the inner NavHostFragment for the 5 main tabs.
 */
class MainContainerFragment : Fragment() {

    private var _binding: FragmentMainContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var innerNavController: NavController

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
        setupBottomNavigation()
        setupBackPress()
        observeDestinationChanges()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_main) as NavHostFragment
        innerNavController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(innerNavController)
    }

    /**
     * Hide bottom nav when entering Chat Detail, show it on all other screens.
     */
    private fun observeDestinationChanges() {
        innerNavController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatDetailFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentDest = innerNavController.currentDestination?.id
                    if (currentDest == R.id.chatDetailFragment) {
                        innerNavController.navigateUp()
                    } else if (currentDest != R.id.homeFragment) {
                        binding.bottomNavigation.selectedItemId = R.id.homeFragment
                    } else {
                        showExitDialog()
                    }
                }
            }
        )
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exit_dialog_title)
            .setMessage(R.string.exit_dialog_message)
            .setPositiveButton(R.string.exit_dialog_confirm) { _, _ ->
                requireActivity().finish()
            }
            .setNegativeButton(R.string.exit_dialog_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
