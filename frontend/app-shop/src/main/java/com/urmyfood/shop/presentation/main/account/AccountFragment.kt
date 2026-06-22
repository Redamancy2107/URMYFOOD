package com.urmyfood.shop.presentation.main.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val manager = ServiceLocator.notificationSettingsManager
        manager.setRequestedPermission(true)

        if (isGranted) {
            manager.setSystemEnabled(true)
            binding.switchNotifications.isChecked = true
            Toast.makeText(requireContext(), "Đã bật thông báo thành công", Toast.LENGTH_SHORT).show()
        } else {
            manager.setSystemEnabled(false)
            binding.switchNotifications.isChecked = false
            Toast.makeText(
                requireContext(),
                "Bạn cần cấp quyền thông báo để nhận cập nhật đơn hàng",
                Toast.LENGTH_SHORT
            ).show()
        }
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
        syncNotificationSwitchState()
        observeRefreshSignal()
        viewModel.loadProfile()
    }

    override fun onResume() {
        super.onResume()
        syncNotificationSwitchState()
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
        binding.tvOpenState.text = if (profile.isOpen) {
            getString(R.string.shop_profile_open)
        } else {
            getString(R.string.shop_profile_closed)
        }
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
        binding.menuNotificationSettings.setOnClickListener {
            handleNotificationToggle()
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

    private fun syncNotificationSwitchState() {
        val isEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ServiceLocator.notificationSettingsManager.isSystemEnabled()
        }
        binding.switchNotifications.isChecked = isEnabled
    }

    private fun handleNotificationToggle() {
        val currentlyEnabled = binding.switchNotifications.isChecked

        if (currentlyEnabled) {
            binding.switchNotifications.isChecked = false
            ServiceLocator.notificationSettingsManager.setSystemEnabled(false)
            Toast.makeText(requireContext(), "Đã tắt nhận thông báo", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                enableNotification()
                return
            }

            val manager = ServiceLocator.notificationSettingsManager

            when {
                !manager.hasRequestedPermission() -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationRationaleDialog()
                }
                else -> {
                    showGoToSettingsDialog()
                }
            }
        } else {
            enableNotification()
        }
    }

    private fun enableNotification() {
        binding.switchNotifications.isChecked = true
        ServiceLocator.notificationSettingsManager.setSystemEnabled(true)
        Toast.makeText(requireContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show()
    }

    private fun showNotificationRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cần quyền thông báo")
            .setMessage(
                "URMYFOOD cần quyền gửi thông báo để cập nhật trạng thái đơn hàng, " +
                "chương trình khuyến mãi và tin nhắn từ khách hàng cho bạn."
            )
            .setPositiveButton("Đồng ý") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Để sau", null)
            .show()
    }

    private fun showGoToSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bật thông báo trong Cài đặt")
            .setMessage(
                "Bạn đã từ chối quyền thông báo trước đó. " +
                "Vui lòng vào Cài đặt ứng dụng → Thông báo để bật lại."
            )
            .setPositiveButton("Mở Cài đặt") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Không thể mở Cài đặt ứng dụng", Toast.LENGTH_SHORT).show()
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
