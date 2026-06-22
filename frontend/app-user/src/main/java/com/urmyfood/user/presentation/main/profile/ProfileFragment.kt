package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        val shouldRefresh = savedStateHandle?.get<Boolean>("refresh_profile") ?: false
        if (shouldRefresh) {
            savedStateHandle.remove<Boolean>("refresh_profile")
        }
        viewModel.loadProfile(force = shouldRefresh)
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

        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileUiState.Loading -> Unit
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    binding.tvUserName.text = profile.fullName
                    binding.tvUserEmail.text = profile.email
                    binding.tvUserPhone.text = profile.phone ?: getString(R.string.profile_no_phone)
                    binding.layoutContactInfo.visibility = View.VISIBLE

                    if (!profile.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileFragment)
                            .load(profile.avatarUrl)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .error(R.drawable.ic_person_placeholder)
                            .into(binding.ivAvatar)
                    } else {
                        binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
                    }
                }
                is ProfileUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ProfileUiState.Idle -> Unit
            }
        }
    }

    private fun setupClickListeners() {
        binding.menuEditProfile.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.profileEditFragment) } }
        binding.menuAddress.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.addressBookFragment) } }
        binding.menuOrderHistory.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.orderHistoryFragment) } }
        binding.menuCoupons.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.vouchersFragment) } }
        binding.menuFavorite.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.favoritesFragment) } }
        binding.menuTermsPolicies.setOnClickListener { findNavController().navigate(R.id.termsPoliciesFragment) }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            navigateToLogin()
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

    private fun navigateToLogin() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.navigate(R.id.loginFragment)
    }

    private fun navigateToRegister() {
        val parentNavController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.let { (it as? androidx.navigation.fragment.NavHostFragment)?.navController }

        parentNavController?.navigate(R.id.signupCustomerFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
