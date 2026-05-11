package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainProfileBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.presentation.common.GuestLoginDialog

class ProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ServiceLocator.provideProfileViewModelFactory()
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
        if (ServiceLocator.guestSessionManager.isGuest()) {
            binding.tvUserName.text = getString(R.string.guest_profile_name)
        } else {
            val fullName = ServiceLocator.tokenManager.getFullName()
            binding.tvUserName.text = fullName ?: getString(R.string.profile_default_name)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { showFeatureInDevelopment() }
        binding.btnSettings.setOnClickListener { showFeatureInDevelopment() }

        binding.menuEditProfile.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuAddress.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuOrderHistory.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuCoupons.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuChangePassword.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }

        binding.btnLogout.setOnClickListener {
            ServiceLocator.tokenManager.clear()
            ServiceLocator.guestSessionManager.clearGuest()
            Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            navigateToAuth()
        }
    }

    private fun showGuestDialogOrRun(action: () -> Unit) {
        if (ServiceLocator.guestSessionManager.isGuest()) {
            showGuestLoginDialog()
        } else {
            action()
        }
    }

    private fun showGuestLoginDialog() {
        val dialog = GuestLoginDialog()
        dialog.onLoginClick = {
            if (isAdded) {
                ServiceLocator.guestSessionManager.clearGuest()
                navigateToAuth()
            }
        }
        dialog.onRegisterClick = {
            if (isAdded) {
                ServiceLocator.guestSessionManager.clearGuest()
                navigateToAuth()
            }
        }
        dialog.show(parentFragmentManager, GuestLoginDialog.TAG)
    }

    private fun navigateToAuth() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.let {
            it.popBackStack(R.id.nav_graph_auth, false)
            it.navigate(R.id.splashFragment)
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
