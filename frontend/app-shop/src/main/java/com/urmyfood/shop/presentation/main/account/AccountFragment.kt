package com.urmyfood.shop.presentation.main.account

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainAccountBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.domain.model.ShopCategory
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.presentation.common.safeNavigate

class AccountFragment : Fragment() {

    private var _binding: FragmentMainAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels {
        ServiceLocator.provideAccountViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
        observeRefreshSignal()
        viewModel.loadProfile()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AccountUiState.Loading -> renderLoading()
                is AccountUiState.Success -> renderProfile(state.profile)
                is AccountUiState.Error -> renderError(state.message)
                is AccountUiState.Idle -> Unit
            }
        }
    }

    private fun renderLoading() {
        Glide.with(this).clear(binding.ivShopCover)
        Glide.with(this).clear(binding.ivShopAvatar)

        binding.ivShopCover.setImageDrawable(null)
        binding.ivShopCover.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.profile_icon_bg_policy))
        binding.ivShopAvatar.setImageResource(R.drawable.ic_person_placeholder)
        binding.tvShopName.text = getString(R.string.shop_profile_loading)
        binding.tvProfileStatus.text = getString(R.string.shop_profile_loading)
        binding.tvCategory.visibility = View.VISIBLE
        binding.tvCategory.text = getString(R.string.shop_profile_loading)
        binding.tvAddress.text = getString(R.string.shop_profile_loading)
        binding.tvOpeningHours.text = getString(R.string.shop_profile_loading)
        binding.tvOpenState.text = ""
    }

    private fun renderProfile(profile: ShopProfile) {
        binding.tvShopName.text = profile.shopName.ifBlank { getString(R.string.shop_profile_default_name) }
        binding.tvProfileStatus.text = verificationText(profile.verificationStatus)
        
        val categoryCode = profile.category
        val categoryEnum = ShopCategory.entries.firstOrNull { it.name == categoryCode || it.displayName == categoryCode }
        if (categoryEnum == null || categoryEnum == ShopCategory.KHAC) {
            binding.tvCategory.visibility = View.GONE
        } else {
            binding.tvCategory.visibility = View.VISIBLE
            binding.tvCategory.text = "Món chủ đạo: ${categoryEnum.displayName}"
        }

        binding.tvAddress.text = profile.address.ifBlank { getString(R.string.shop_profile_no_address) }
        binding.tvOpeningHours.text = profile.openingHours.ifBlank { getString(R.string.shop_profile_default_hours) }
        val isActuallyOpen = com.urmyfood.shared.util.TimeUtils.isShopCurrentlyOpen(profile.isOpen, profile.openingHours)
        binding.tvOpenState.text = if (isActuallyOpen) {
            getString(R.string.shop_profile_open)
        } else {
            getString(R.string.shop_profile_closed)
        }
        binding.tvOpenState.setTextColor(if (isActuallyOpen) android.graphics.Color.parseColor("#10B981") else android.graphics.Color.parseColor("#EF4444"))
        binding.ivShopCover.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        if (profile.coverUrl.isNullOrBlank()) {
            binding.ivShopCover.setImageResource(R.drawable.bg_food_banner)
        } else {
            Glide.with(this)
                .load(profile.coverUrl)
                .placeholder(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.profile_icon_bg_policy)))
                .error(R.drawable.bg_food_banner)
                .into(binding.ivShopCover)
        }

        Glide.with(this)
            .load(profile.logoUrl)
            .placeholder(R.drawable.ic_person_placeholder)
            .error(R.drawable.ic_person_placeholder)
            .into(binding.ivShopAvatar)
    }

    private fun renderError(message: String) {
        Glide.with(this).clear(binding.ivShopCover)
        Glide.with(this).clear(binding.ivShopAvatar)

        binding.ivShopCover.setImageDrawable(null)
        binding.ivShopCover.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.profile_icon_bg_policy))
        binding.ivShopAvatar.setImageResource(R.drawable.ic_person_placeholder)
        binding.tvProfileStatus.text = message
        binding.tvShopName.text = getString(R.string.shop_profile_default_name)
        binding.tvCategory.visibility = View.VISIBLE
        binding.tvCategory.text = getString(R.string.shop_profile_unknown_category)
        binding.tvAddress.text = getString(R.string.shop_profile_no_address)
        binding.tvOpeningHours.text = getString(R.string.shop_profile_default_hours)
        binding.tvOpenState.text = ""
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun verificationText(status: String): String {
        return when (status) {
            "APPROVED" -> getString(R.string.shop_profile_verified)
            "PENDING" -> getString(R.string.shop_profile_pending)
            "REJECTED" -> getString(R.string.shop_profile_rejected)
            else -> getString(R.string.shop_profile_not_submitted)
        }
    }

    private fun categoryDisplayName(categoryCode: String): String {
        return ShopCategory.entries
            .firstOrNull { it.name == categoryCode || it.displayName == categoryCode }
            ?.displayName
            ?: getString(R.string.shop_profile_unknown_category)
    }

    private fun observeRefreshSignal() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        savedStateHandle.getLiveData<Boolean>("refresh_profile").observe(viewLifecycleOwner) { shouldRefresh ->
            if (shouldRefresh) {
                savedStateHandle["refresh_profile"] = false
                viewModel.loadProfile()
            }
        }
    }

    private fun setupClickListeners() {
        binding.menuEditProfile.setOnClickListener {
            findNavController().safeNavigate(R.id.action_account_to_shopProfileEditFragment)
        }
        binding.menuStatistics.setOnClickListener {
            findNavController().safeNavigate(R.id.action_account_to_statistics)
        }
        binding.menuTermsPolicies.setOnClickListener {
            findNavController().safeNavigate(R.id.action_account_to_termsPoliciesFragment)
        }
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.account_logout_confirm_title)
            .setMessage(R.string.account_logout_confirm_message)
            .setPositiveButton(R.string.account_logout_confirm_yes) { _, _ ->
                viewModel.logout()
                requireActivity().let {
                    it.finish()
                    it.startActivity(it.intent)
                }
            }
            .setNegativeButton(R.string.account_logout_confirm_no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
