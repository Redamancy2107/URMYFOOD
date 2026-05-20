package com.urmyfood.user.presentation.main.profile

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
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainProfileBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.presentation.common.GuestLoginDialog

/**
 * Màn hình Hồ sơ cá nhân (Profile).
 * Quản lý hiển thị thông tin user, avatar, cài đặt thông báo và đăng xuất.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ServiceLocator.provideProfileViewModelFactory()
    }

    // ==================== NOTIFICATION PERMISSION ====================

    /**
     * Launcher xin quyền POST_NOTIFICATIONS (Android 13+).
     * Kết quả trả về từ hệ thống sẽ được xử lý tại đây.
     */
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

    // ==================== LIFECYCLE ====================

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
        syncNotificationSwitchState()
        viewModel.loadProfile()
    }

    override fun onResume() {
        super.onResume()
        // Đồng bộ lại switch khi user quay về từ Settings (có thể đã bật/tắt quyền ở đó)
        syncNotificationSwitchState()
    }

    // ==================== NOTIFICATION PERMISSION LOGIC ====================

    /**
     * Đồng bộ trạng thái switch thông báo dựa trên quyền thực tế của hệ thống.
     * - Android 13+ (API 33): Kiểm tra quyền POST_NOTIFICATIONS
     * - Android < 13: Đọc từ SharedPreferences vì không cần quyền runtime
     */
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

    /**
     * Xử lý khi user bấm vào dòng "Cài đặt thông báo".
     *
     * Flow xin quyền giống lấy quyền danh bạ:
     *
     * 1. Nếu đang BẬT → tắt đi, không cần xin quyền.
     * 2. Nếu đang TẮT → muốn BẬT:
     *    a. Android < 13: Không cần runtime permission, bật trực tiếp.
     *    b. Android 13+:
     *       - Đã có quyền rồi → bật trực tiếp.
     *       - Chưa có quyền:
     *         + Chưa từng xin → Hiện dialog hệ thống lần đầu.
     *         + Đã từng xin, hệ thống còn cho hỏi (rationale) → Hiện dialog giải thích rồi xin lại.
     *         + Đã từng từ chối vĩnh viễn ("Don't ask again") → Mở Settings app.
     */
    private fun handleNotificationToggle() {
        val currentlyEnabled = binding.switchNotifications.isChecked

        if (currentlyEnabled) {
            // User muốn TẮT → tắt trực tiếp
            binding.switchNotifications.isChecked = false
            ServiceLocator.notificationSettingsManager.setSystemEnabled(false)
            Toast.makeText(requireContext(), "Đã tắt nhận thông báo", Toast.LENGTH_SHORT).show()
            return
        }

        // User muốn BẬT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                // Đã có quyền → bật trực tiếp
                enableNotification()
                return
            }

            val manager = ServiceLocator.notificationSettingsManager

            when {
                // Trường hợp 1: Chưa từng xin quyền → Xin quyền lần đầu (dialog hệ thống)
                !manager.hasRequestedPermission() -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                // Trường hợp 2: Đã từng từ chối nhưng hệ thống vẫn cho hỏi lại
                // → Hiện dialog giải thích lý do (rationale) rồi xin lại
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationRationaleDialog()
                }

                // Trường hợp 3: User đã chọn "Don't ask again" hoặc hệ thống không cho hỏi nữa
                // → Mở Settings app để user tự bật
                else -> {
                    showGoToSettingsDialog()
                }
            }
        } else {
            // Android < 13: Không cần xin quyền runtime
            enableNotification()
        }
    }

    /**
     * Bật thông báo (cập nhật switch + lưu preferences).
     */
    private fun enableNotification() {
        binding.switchNotifications.isChecked = true
        ServiceLocator.notificationSettingsManager.setSystemEnabled(true)
        Toast.makeText(requireContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show()
    }

    /**
     * Hiện AlertDialog giải thích lý do cần quyền thông báo (rationale).
     * Khi user đồng ý → xin quyền lại qua dialog hệ thống.
     */
    private fun showNotificationRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cần quyền thông báo")
            .setMessage(
                "URMYFOOD cần quyền gửi thông báo để cập nhật trạng thái đơn hàng, " +
                "chương trình khuyến mãi và tin nhắn từ cửa hàng cho bạn."
            )
            .setPositiveButton("Đồng ý") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Để sau", null)
            .show()
    }

    /**
     * Hiện AlertDialog hướng dẫn user vào Settings để bật quyền thủ công.
     * Dùng khi user đã chọn "Don't ask again" nên hệ thống không cho hiện dialog xin quyền nữa.
     */
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

    /**
     * Mở màn hình Settings chi tiết của ứng dụng.
     * User có thể bật/tắt quyền thông báo tại đây.
     */
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

    // ==================== OBSERVE VIEW MODEL ====================

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

    // ==================== CLICK LISTENERS ====================

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener { showFeatureInDevelopment() }

        // Quick Actions - navigate to sub-screens
        binding.menuEditProfile.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.profileEditFragment) } }
        binding.menuAddress.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.addressBookFragment) } }
        binding.menuOrderHistory.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.orderHistoryFragment) } }
        binding.menuCoupons.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.vouchersFragment) } }
        binding.menuFavorite.setOnClickListener { showGuestDialogOrRun { findNavController().navigate(R.id.favoritesFragment) } }

        // Notification toggle
        binding.menuNotificationSettings.setOnClickListener {
            showGuestDialogOrRun { handleNotificationToggle() }
        }

        binding.menuTermsPolicies.setOnClickListener { findNavController().navigate(R.id.termsPoliciesFragment) }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    // ==================== HELPER METHODS ====================

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
