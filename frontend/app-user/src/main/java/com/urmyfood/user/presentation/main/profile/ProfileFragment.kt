package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainProfileBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Profile screen fragment.
 * Displays user info, settings menu items, and logout button.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideProfileViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupClickListeners()
    }

    private fun setupUserInfo() {
        // Display user's name from TokenManager
        val fullName = ServiceLocator.tokenManager.getFullName()
        binding.tvUserName.text = fullName ?: getString(R.string.profile_default_name)
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            showFeatureInDevelopment()
        }

        // Settings button → feature in development
        binding.btnSettings.setOnClickListener {
            showFeatureInDevelopment()
        }

        // Menu items → all feature in development
        binding.menuEditProfile.setOnClickListener { showFeatureInDevelopment() }
        binding.menuAddress.setOnClickListener { showFeatureInDevelopment() }
        binding.menuOrderHistory.setOnClickListener { showFeatureInDevelopment() }
        binding.menuCoupons.setOnClickListener { showFeatureInDevelopment() }
        binding.menuChangePassword.setOnClickListener { showFeatureInDevelopment() }

        // Logout button
        binding.btnLogout.setOnClickListener {
            ServiceLocator.tokenManager.clear()
            Toast.makeText(
                requireContext(),
                "Đã đăng xuất thành công",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate back to auth flow
            // Pop up to the MainContainerFragment and go back to auth
            val parentNavController = requireActivity()
                .supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment)
                ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

            parentNavController?.let {
                it.popBackStack(R.id.nav_graph_auth, false)
                it.navigate(R.id.splashFragment)
            }
        }
    }

    private fun showFeatureInDevelopment() {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
