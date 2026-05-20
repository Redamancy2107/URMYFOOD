package com.urmyfood.user.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
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

    /** IDs of the 5 root tab destinations */
    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.searchFragment,
        R.id.cartFragment,
        R.id.chatFragment,
        R.id.profileFragment
    )

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

    /**
     * Custom tab selection: always navigate to the root destination of the
     * selected tab, clearing any sub-screen back-stack. This prevents the
     * bug where re-selecting a tab shows a stale sub-screen instead of the
     * root screen.
     */
    private fun setupBottomNavigation() {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_main) as NavHostFragment
        innerNavController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == innerNavController.currentDestination?.id) {
                // Already on this root destination – do nothing
                return@setOnItemSelectedListener true
            }

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph_main, inclusive = false)
                .setLaunchSingleTop(true)
                .build()

            innerNavController.navigate(item.itemId, null, navOptions)
            true
        }

        // Sync the selected tab icon when the destination changes
        innerNavController.addOnDestinationChangedListener { _, destination, _ ->
            val matchingTab = when (destination.id) {
                R.id.homeFragment -> R.id.homeFragment
                R.id.searchFragment -> R.id.searchFragment
                R.id.cartFragment -> R.id.cartFragment
                R.id.chatFragment, R.id.chatDetailFragment -> R.id.chatFragment
                else -> R.id.profileFragment // profile sub-screens highlight Profile tab
            }
            if (binding.bottomNavigation.selectedItemId != matchingTab) {
                binding.bottomNavigation.menu.findItem(matchingTab)?.isChecked = true
            }
        }
    }

    /**
     * Hide bottom nav when entering Chat Detail or profile sub-screens
     * that have their own header; show it on all other screens.
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

    /**
     * Back press logic:
     * 1. If on a sub-screen (not a top-level tab), pop back.
     * 2. If on a top-level tab other than Home, switch to Home.
     * 3. If on Home, show exit dialog.
     */
    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentDest = innerNavController.currentDestination?.id
                    if (currentDest != null && currentDest !in topLevelDestinations) {
                        // Sub-screen: pop back to the parent tab
                        innerNavController.navigateUp()
                    } else if (currentDest != R.id.homeFragment) {
                        // Non-home top-level tab: go back to Home
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
