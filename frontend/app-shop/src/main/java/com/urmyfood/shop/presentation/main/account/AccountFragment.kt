package com.urmyfood.shop.presentation.main.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainAccountBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.common.safeNavigate

class AccountFragment : Fragment() {

    private var _binding: FragmentMainAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels()

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
    }

    override fun onResume() {
        super.onResume()
        syncNotificationSwitchState()
    }

    private fun observeViewModel() {
        viewModel.shopName.observe(viewLifecycleOwner) { name ->
            binding.tvShopName.text = name
        }
        viewModel.shopRating.observe(viewLifecycleOwner) { rating ->
            binding.tvRating.text = rating.toString()
        }
        viewModel.ratingCount.observe(viewLifecycleOwner) { count ->
            binding.tvRatingCount.text = getString(R.string.account_rating_count_format, count)
        }
    }

    private fun setupClickListeners() {
        binding.menuNotificationSettings.setOnClickListener {
            handleNotificationToggle()
        }
        binding.menuBusinessHours.setOnClickListener {
            findNavController().safeNavigate(R.id.action_account_to_businessHours)
        }
        binding.menuStatistics.setOnClickListener {
            findNavController().safeNavigate(R.id.action_account_to_statistics)
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
