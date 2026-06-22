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
import com.urmyfood.admin.R

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
        // Stat cards
        try {
            binding.tvTotalRevenue.text = currencyFormat.format(data.totalRevenue)
            binding.tvNewOrders.text = data.newOrders.toString()
            binding.tvNewUsers.text = data.newUsers.toString()
            binding.tvActiveShops.text = data.activeShops.toString()

            val inflater = LayoutInflater.from(requireContext())
            
            // Recent Activities
            val llActivities = binding.root.findViewById<android.widget.LinearLayout>(R.id.ll_recent_activities)
            llActivities?.let {
                it.removeAllViews()
                data.recentActivities.forEach { activity ->
                    val view = inflater.inflate(R.layout.item_recent_activity, it, false)
                    val tvTitle = view.findViewById<android.widget.TextView>(R.id.tv_activity_title)
                    val tvDesc = view.findViewById<android.widget.TextView>(R.id.tv_activity_desc)
                    val tvStatus = view.findViewById<android.widget.TextView>(R.id.tv_activity_status)
                    val ivIcon = view.findViewById<android.widget.ImageView>(R.id.iv_activity_icon)

                    tvTitle.text = activity.description
                    tvDesc.text = "Loại: ${activity.type} - Lúc: ${activity.time}"
                    
                    if (activity.type == "NEW_VERIFICATION") {
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = "ĐANG XỬ LÝ"
                    } else {
                        tvStatus.visibility = View.GONE
                    }

                    when (activity.type) {
                        "NEW_USER" -> ivIcon.setImageResource(android.R.drawable.ic_menu_myplaces)
                        "NEW_ORDER" -> ivIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                        "NEW_VERIFICATION" -> ivIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                        else -> ivIcon.setImageResource(android.R.drawable.sym_def_app_icon)
                    }

                    it.addView(view)
                }
            }

            // Latest Shops
            val llShops = binding.root.findViewById<android.widget.LinearLayout>(R.id.ll_latest_shops)
            llShops?.let {
                it.removeAllViews()
                data.latestShops.forEach { shop ->
                    val view = inflater.inflate(R.layout.item_latest_shop, it, false)
                    val tvName = view.findViewById<android.widget.TextView>(R.id.tv_shop_name)
                    val tvOwner = view.findViewById<android.widget.TextView>(R.id.tv_shop_owner)
                    val tvCategory = view.findViewById<android.widget.TextView>(R.id.tv_shop_category)
                    val tvRevenue = view.findViewById<android.widget.TextView>(R.id.tv_shop_revenue)
                    val tvStatus = view.findViewById<android.widget.TextView>(R.id.tv_shop_status)

                    tvName.text = shop.shopName ?: "Chưa cập nhật"
                    tvOwner.text = shop.email ?: "Không có email"
                    tvCategory.text = "Đang cập nhật" // We don't have category in LatestShop model, so placeholder
                    tvRevenue.text = "---" // Same for revenue
                    
                    val statusText = if (shop.status == "APPROVED") "Hoạt động" else if (shop.status == "PENDING") "Chờ duyệt" else "Chưa duyệt"
                    tvStatus.text = "● $statusText"
                    
                    val context = requireContext()
                    if (shop.status == "APPROVED") {
                        tvStatus.setTextColor(context.getColor(R.color.mint_green))
                    } else {
                        tvStatus.setTextColor(context.getColor(R.color.text_secondary))
                    }

                    it.addView(view)
                }
            }
        } catch (_: Exception) {
            // Views may not exist in the current layout, that's ok
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
