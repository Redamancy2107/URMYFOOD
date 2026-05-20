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
import androidx.navigation.fragment.findNavController
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
        observeViewModel()
        setupClickListeners()
        binding.switchNotifications.isChecked = ServiceLocator.notificationSettingsManager.isSystemEnabled()
        viewModel.loadProfile()
    }

    private fun observeViewModel() {
        viewModel.isGuest.observe(viewLifecycleOwner) { isGuest ->
            if (isGuest) {
                binding.tvUserName.text = getString(R.string.guest_profile_name)
                binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
                binding.btnLogout.visibility = View.GONE
                binding.layoutContactInfo.visibility = View.GONE
            } else {
                binding.btnLogout.visibility = View.VISIBLE
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            if (name != null) {
                binding.tvUserName.text = name
            } else if (viewModel.isGuest.value != true) {
                binding.tvUserName.text = getString(R.string.profile_default_name)
            }
        }

        viewModel.isStudentVerified.observe(viewLifecycleOwner) { isVerified ->
            if (isVerified) {
                binding.tvVerifyStatus.text = getString(R.string.profile_student_verified)
                binding.tvVerifyStatus.setTextColor(resources.getColor(R.color.white, null))
                binding.badgeVerified.setBackgroundResource(R.drawable.bg_badge_verified)
                binding.ivVerifyIcon.setImageResource(R.drawable.ic_favorite)
                binding.ivVerifyIcon.setColorFilter(resources.getColor(R.color.white, null))
            } else {
                binding.tvVerifyStatus.text = getString(R.string.profile_student_unverified)
                binding.tvVerifyStatus.setTextColor(android.graphics.Color.parseColor("#D97706"))
                binding.badgeVerified.setBackgroundResource(R.drawable.bg_badge_unverified)
                binding.ivVerifyIcon.setImageResource(R.drawable.ic_favorite)
                binding.ivVerifyIcon.setColorFilter(android.graphics.Color.parseColor("#D97706"))
            }
        }

        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileUiState.Loading -> {
                    // Name from TokenManager is already shown; no full-screen loading needed
                }
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    binding.tvUserName.text = profile.fullName
                    binding.tvUserEmail.text = profile.email
                    binding.tvUserPhone.text = profile.phone ?: getString(R.string.profile_no_phone)
                    binding.layoutContactInfo.visibility = View.VISIBLE
                }
                is ProfileUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ProfileUiState.Idle -> Unit
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener { showFeatureInDevelopment() }

        // Quick Actions - navigate to sub-screens
        binding.menuEditProfile.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.profileEditFragment) } }
        binding.menuAddress.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.addressBookFragment) } }
        binding.menuOrderHistory.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.orderHistoryFragment) } }
        binding.menuCoupons.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.vouchersFragment) } }
        binding.menuFavorite.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.favoritesFragment) } }

        binding.menuNotificationSettings.setOnClickListener {
            showGuestDialogOrRun {
                val nextState = !binding.switchNotifications.isChecked
                binding.switchNotifications.isChecked = nextState
                ServiceLocator.notificationSettingsManager.setSystemEnabled(nextState)
                Toast.makeText(
                    requireContext(),
                    if (nextState) "Đã bật thông báo" else "Đã tắt nhận thông báo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.menuTermsPolicies.setOnClickListener { findNavController().navigate(R.id.termsPoliciesFragment) }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            navigateToChooseRole()
        }
    }

    private fun showGuestDialogOrRun(action: () -> Unit) {
        if (viewModel.isGuest.value == true) {
            showGuestLoginDialog()
        } else {
            action()
        }
    }

    private fun showGuestLoginDialog() {
        val dialog = GuestLoginDialog()
        dialog.onLoginClick = {
            if (isAdded) {
                viewModel.logout()
                navigateToLogin()
            }
        }
        dialog.onRegisterClick = {
            if (isAdded) {
                viewModel.logout()
                navigateToRegister()
            }
        }
        dialog.show(parentFragmentManager, GuestLoginDialog.TAG)
    }

    private fun navigateToChooseRole() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.let {
            it.navigate(R.id.chooseRoleFragment)
        }
    }

    private fun navigateToLogin() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.let {
            it.navigate(R.id.loginFragment)
        }
    }

    private fun navigateToRegister() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.let {
            it.navigate(R.id.signupCustomerFragment)
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
