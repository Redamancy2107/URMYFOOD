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
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.isGuest.observe(viewLifecycleOwner) { isGuest ->
            if (isGuest) {
                binding.tvUserName.text = getString(R.string.guest_profile_name)
                binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
                binding.btnLogout.visibility = View.GONE
            } else {
                binding.btnLogout.visibility = View.VISIBLE
                // For logged in users, we might want to load their real avatar
                // binding.ivAvatar.load(user.avatarUrl) 
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
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener { showFeatureInDevelopment() }

        binding.menuEditProfile.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuAddress.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuOrderHistory.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuCoupons.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        
        binding.menuNotificationSettings.setOnClickListener { showGuestDialogOrRun { showFeatureInDevelopment() } }
        binding.menuSupportCenter.setOnClickListener { showFeatureInDevelopment() }
        binding.menuTermsPolicies.setOnClickListener { showFeatureInDevelopment() }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            navigateToAuth()
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
                navigateToAuth()
            }
        }
        dialog.onRegisterClick = {
            if (isAdded) {
                viewModel.logout()
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
