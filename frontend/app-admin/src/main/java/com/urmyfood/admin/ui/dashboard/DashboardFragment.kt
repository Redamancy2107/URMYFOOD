package com.urmyfood.admin.ui.dashboard

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.admin.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private var mediaPlayer: MediaPlayer? = null
    private var isMusicPlaying = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val showDevToast = { _: View ->
            Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // Setup Sidebar clicks
        binding.menuTongQuan.setOnClickListener {
            switchFragment(OverviewFragment())
            updateMenuSelection(binding.menuTongQuan)
        }
        binding.menuDuyetDoiTac.setOnClickListener(showDevToast)
        binding.menuKiemDuyet.setOnClickListener(showDevToast)
        binding.menuQuanLyUser.setOnClickListener(showDevToast)
        binding.menuQuanLyCuaHang.setOnClickListener(showDevToast)
        binding.menuMaGiamGia.setOnClickListener(showDevToast)
        binding.menuBaoCao.setOnClickListener(showDevToast)
        binding.menuCaiDat.setOnClickListener {
            switchFragment(SettingsFragment())
            updateMenuSelection(binding.menuCaiDat)
        }
        binding.btnXuatBaoCao.setOnClickListener(showDevToast)
        binding.llHoTro.setOnClickListener(showDevToast)
        binding.llDangXuat.setOnClickListener {
            // Simple navigation back to login (mocking logout)
            findNavController().navigateUp()
        }

        // Topbar clicks
        binding.tvTabHomNay.setOnClickListener(showDevToast)
        binding.tvTabTuanNay.setOnClickListener(showDevToast)
        binding.tvTabThangNay.setOnClickListener(showDevToast)
        binding.ivNoti.setOnClickListener(showDevToast)
        binding.ivMessage.setOnClickListener(showDevToast)
        
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
        
        menus.forEach { menu ->
            if (menu == selectedView) {
                menu.setBackgroundResource(com.urmyfood.admin.R.drawable.bg_menu_item_active)
            } else {
                menu.setBackgroundResource(android.R.color.transparent)
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
}
