package com.urmyfood.shop.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainContainerBinding
import com.urmyfood.shop.presentation.common.safeNavigate

/**
 * Main container fragment that hosts the BottomNavigationView
 * and an inner NavHostFragment for the 5 shop tabs.
 */
class MainContainerFragment : Fragment() {

    private var _binding: FragmentMainContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var innerNavController: NavController

    /** IDs of the 5 root tab destinations */
    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.postsFragment,
        R.id.ordersFragment,
        R.id.chatFragment,
        R.id.accountFragment
    )

    /** Sub-screens where the bottom nav should be hidden */
    private val hideBottomNavDestinations = setOf(
        R.id.orderDetailFragment,
        R.id.createPostFragment,
        R.id.statisticsFragment,
        R.id.businessHoursFragment,
        R.id.chatDetailFragment
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
     * Custom tab selection: navigate to the root destination of the
     * selected tab, clearing any sub-screen back-stack.
     */
    private fun setupBottomNavigation() {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_main) as NavHostFragment
        innerNavController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == innerNavController.currentDestination?.id) {
                return@setOnItemSelectedListener true
            }

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph_main, inclusive = false)
                .setLaunchSingleTop(true)
                .build()

            innerNavController.navigate(item.itemId, null, navOptions)
            true
        }
    }

    /**
     * Sync selected tab icon when destination changes,
     * and hide/show bottom nav for sub-screens.
     */
    private fun observeDestinationChanges() {
        innerNavController.addOnDestinationChangedListener { _, destination, _ ->
            // Sync tab highlight
            val matchingTab = when (destination.id) {
                R.id.homeFragment -> R.id.homeFragment
                R.id.postsFragment, R.id.createPostFragment -> R.id.postsFragment
                R.id.ordersFragment, R.id.orderDetailFragment -> R.id.ordersFragment
                R.id.chatFragment -> R.id.chatFragment
                else -> R.id.accountFragment
            }
            if (binding.bottomNavigation.selectedItemId != matchingTab) {
                binding.bottomNavigation.menu.findItem(matchingTab)?.isChecked = true
            }

            // Show/hide bottom nav
            binding.bottomNavigation.visibility =
                if (destination.id in hideBottomNavDestinations) View.GONE else View.VISIBLE
        }
    }

    /**
     * Back press logic:
     * 1. If on a sub-screen (not top-level), pop back.
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
