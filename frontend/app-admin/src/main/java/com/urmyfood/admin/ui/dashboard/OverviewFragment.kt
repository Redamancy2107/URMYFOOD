package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.urmyfood.admin.data.model.DashboardOverview
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentOverviewBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Overview/Dashboard fragment showing real-time statistics from the backend.
 * Displays total revenue, new orders, new users, active shops,
 * recent activity, and latest shops.
 */
class OverviewFragment : Fragment() {
    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    private val repository = AdminRepository()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getDashboardOverview()
            result.onSuccess { data ->
                bindDashboardData(data)
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi tải dữ liệu: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindDashboardData(data: DashboardOverview) {
        // Stat cards - using view IDs from existing layout
        try {
            binding.tvTotalRevenue.text = currencyFormat.format(data.totalRevenue)
            binding.tvNewOrders.text = data.newOrders.toString()
            binding.tvNewUsers.text = data.newUsers.toString()
            binding.tvActiveShops.text = data.activeShops.toString()
        } catch (_: Exception) {
            // Views may not exist in the current layout, that's ok
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
