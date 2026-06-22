package com.urmyfood.admin.ui.dashboard

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.urmyfood.admin.R
import com.urmyfood.admin.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private var mediaPlayer: MediaPlayer? = null
    private var isMusicPlaying = true
    private val repository = com.urmyfood.admin.data.repository.AdminRepository()
    private var currentSelectedTab: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadAdminProfileHeader()
        
        val showDevToast = { _: View ->
            Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // Setup Sidebar clicks
        binding.menuTongQuan.setOnClickListener {
            if (currentSelectedTab == binding.menuTongQuan) return@setOnClickListener
            switchFragment(OverviewFragment())
            updateMenuSelection(binding.menuTongQuan)
        }
        binding.menuDuyetDoiTac.setOnClickListener {
            if (currentSelectedTab == binding.menuDuyetDoiTac) return@setOnClickListener
            switchFragment(PartnerApprovalFragment())
            updateMenuSelection(binding.menuDuyetDoiTac)
        }
        binding.menuKiemDuyet.setOnClickListener {
            if (currentSelectedTab == binding.menuKiemDuyet) return@setOnClickListener
            switchFragment(ModerationFragment())
            updateMenuSelection(binding.menuKiemDuyet)
        }
        binding.menuQuanLyUser.setOnClickListener {
            if (currentSelectedTab == binding.menuQuanLyUser) return@setOnClickListener
            switchFragment(UserManagementFragment.newInstance("CUSTOMER"))
            updateMenuSelection(binding.menuQuanLyUser)
        }
        binding.menuQuanLyCuaHang.setOnClickListener {
            if (currentSelectedTab == binding.menuQuanLyCuaHang) return@setOnClickListener
            switchFragment(UserManagementFragment.newInstance("SHOP"))
            updateMenuSelection(binding.menuQuanLyCuaHang)
        }
        binding.menuMaGiamGia.setOnClickListener {
            if (currentSelectedTab == binding.menuMaGiamGia) return@setOnClickListener
            switchFragment(VoucherManagementFragment())
            updateMenuSelection(binding.menuMaGiamGia)
        }
        binding.menuBaoCao.setOnClickListener {
            if (currentSelectedTab == binding.menuBaoCao) return@setOnClickListener
            switchFragment(ReportsFragment())
            updateMenuSelection(binding.menuBaoCao)
        }
        binding.menuCaiDat.setOnClickListener {
            if (currentSelectedTab == binding.menuCaiDat) return@setOnClickListener
            switchFragment(SettingsFragment())
            updateMenuSelection(binding.menuCaiDat)
        }

        binding.llDangXuat.setOnClickListener {
            com.urmyfood.admin.data.local.SessionManager.clearSession()
            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
        }

        // Topbar clicks
        binding.ivNoti.setOnClickListener(showDevToast)
        
        // Setup background music
        setupBackgroundMusic()

        // Load initial fragment
        if (savedInstanceState == null) {
            switchFragment(OverviewFragment())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(com.urmyfood.admin.R.id.dashboard_content_frame, fragment)
            .commit()
    }

    private fun updateMenuSelection(selectedView: View) {
        val menus = listOf(
            binding.menuTongQuan, binding.menuDuyetDoiTac, binding.menuKiemDuyet,
            binding.menuQuanLyUser, binding.menuQuanLyCuaHang, binding.menuMaGiamGia,
            binding.menuBaoCao, binding.menuCaiDat
        )
        
        val activeColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.urmyfood.admin.R.color.dark_green)
        val inactiveColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.urmyfood.admin.R.color.text_secondary)
        
        menus.forEach { menu ->
            val imageView = menu.getChildAt(0) as? android.widget.ImageView
            val textView = menu.getChildAt(1) as? android.widget.TextView
            
            if (menu == selectedView) {
                menu.setBackgroundResource(com.urmyfood.admin.R.drawable.bg_menu_item_active)
                imageView?.imageTintList = android.content.res.ColorStateList.valueOf(activeColor)
                textView?.setTextColor(activeColor)
                textView?.setTypeface(null, android.graphics.Typeface.BOLD)
                currentSelectedTab = selectedView
            } else {
                menu.setBackgroundResource(android.R.color.transparent)
                imageView?.imageTintList = android.content.res.ColorStateList.valueOf(inactiveColor)
                textView?.setTextColor(inactiveColor)
                textView?.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
    
    private fun setupBackgroundMusic() {
        mediaPlayer = MediaPlayer.create(requireContext(), com.urmyfood.admin.R.raw.bg_music)
        mediaPlayer?.isLooping = true
        
        if (isMusicPlaying) {
            mediaPlayer?.start()
            binding.ivSoundToggle.setImageResource(com.urmyfood.admin.R.drawable.ic_volume_up)
        } else {
            binding.ivSoundToggle.setImageResource(com.urmyfood.admin.R.drawable.ic_volume_off)
        }
        
        binding.ivSoundToggle.setOnClickListener {
            if (isMusicPlaying) {
                // Pause music
                mediaPlayer?.pause()
                binding.ivSoundToggle.setImageResource(com.urmyfood.admin.R.drawable.ic_volume_off)
                isMusicPlaying = false
            } else {
                // Play music
                mediaPlayer?.start()
                binding.ivSoundToggle.setImageResource(com.urmyfood.admin.R.drawable.ic_volume_up)
                isMusicPlaying = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMusicPlaying) {
            mediaPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }

    fun updateAdminHeader(name: String, avatarUrl: String?) {
        if (_binding == null) return
        binding.tvAdminNameTop.text = name
        binding.ivAdminAvatarTop.setImageResource(R.drawable.ic_logo_admin)
    }

    private fun loadAdminProfileHeader() {
        lifecycleScope.launch {
            val result = repository.getAdminProfile()
            result.onSuccess { profile ->
                updateAdminHeader(profile.fullName ?: "Admin", profile.avatarUrl)
            }
        }
    }
}
