package com.urmyfood.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.urmyfood.admin.R
import com.urmyfood.admin.data.model.CustomerReport
import com.urmyfood.admin.data.model.StoreReport
import com.urmyfood.admin.data.repository.AdminRepository
import com.urmyfood.admin.databinding.FragmentReportsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val repository = AdminRepository()
    private val storeReports = mutableListOf<StoreReport>()
    private val customerReports = mutableListOf<CustomerReport>()
    private var isStoreTabActive = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupTabs()
        loadReports()
    }

    private fun setupRecyclerView() {
        binding.rvReports.layoutManager = LinearLayoutManager(requireContext())
        updateAdapter()
    }

    private fun setupTabs() {
        binding.tvTabStores.setOnClickListener {
            if (!isStoreTabActive) {
                isStoreTabActive = true
                binding.tvTabStores.setBackgroundResource(R.drawable.bg_menu_item_active)
                binding.tvTabStores.setTextColor(resources.getColor(R.color.dark_green, null))
                
                binding.tvTabCustomers.setBackgroundResource(android.R.color.transparent)
                binding.tvTabCustomers.setTextColor(resources.getColor(R.color.text_secondary, null))

                binding.llStoreHeader.visibility = View.VISIBLE
                binding.llCustomerHeader.visibility = View.GONE
                updateAdapter()
                loadReports()
            }
        }

        binding.tvTabCustomers.setOnClickListener {
            if (isStoreTabActive) {
                isStoreTabActive = false
                binding.tvTabCustomers.setBackgroundResource(R.drawable.bg_menu_item_active)
                binding.tvTabCustomers.setTextColor(resources.getColor(R.color.dark_green, null))
                
                binding.tvTabStores.setBackgroundResource(android.R.color.transparent)
                binding.tvTabStores.setTextColor(resources.getColor(R.color.text_secondary, null))

                binding.llCustomerHeader.visibility = View.VISIBLE
                binding.llStoreHeader.visibility = View.GONE
                updateAdapter()
                loadReports()
            }
        }
    }

    private fun updateAdapter() {
        if (isStoreTabActive) {
            binding.rvReports.adapter = StoreReportAdapter(storeReports)
        } else {
            binding.rvReports.adapter = CustomerReportAdapter(customerReports)
        }
    }

    private fun loadReports() {
        lifecycleScope.launch {
            if (isStoreTabActive) {
                val result = repository.getStoreReports()
                result.onSuccess { list ->
                    storeReports.clear()
                    storeReports.addAll(list)
                    binding.rvReports.adapter?.notifyDataSetChanged()
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Lỗi tải báo cáo cửa hàng: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                val result = repository.getCustomerReports()
                result.onSuccess { list ->
                    customerReports.clear()
                    customerReports.addAll(list)
                    binding.rvReports.adapter?.notifyDataSetChanged()
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Lỗi tải báo cáo khách hàng: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Store Report Adapter
    private class StoreReportAdapter(private val items: List<StoreReport>) :
        RecyclerView.Adapter<StoreReportAdapter.ViewHolder>() {

        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val shopName: TextView = view.findViewById(R.id.tv_shop_name)
            val email: TextView = view.findViewById(R.id.tv_email)
            val revenue: TextView = view.findViewById(R.id.tv_revenue)
            val totalOrders: TextView = view.findViewById(R.id.tv_total_orders)
            val completedOrders: TextView = view.findViewById(R.id.tv_completed_orders)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store_report, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.shopName.text = item.shopName
            holder.email.text = item.email ?: "N/A"
            holder.revenue.text = currencyFormat.format(item.totalRevenue)
            holder.totalOrders.text = "${item.totalOrders} đơn"
            holder.completedOrders.text = "${item.completedOrders} đơn"
        }

        override fun getItemCount() = items.size
    }

    // Customer Report Adapter
    private class CustomerReportAdapter(private val items: List<CustomerReport>) :
        RecyclerView.Adapter<CustomerReportAdapter.ViewHolder>() {

        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val customerName: TextView = view.findViewById(R.id.tv_customer_name)
            val email: TextView = view.findViewById(R.id.tv_email)
            val spent: TextView = view.findViewById(R.id.tv_spent)
            val totalOrders: TextView = view.findViewById(R.id.tv_total_orders)
            val completedOrders: TextView = view.findViewById(R.id.tv_completed_orders)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer_report, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.customerName.text = item.fullName
            holder.email.text = item.email ?: "N/A"
            holder.spent.text = currencyFormat.format(item.totalSpent)
            holder.totalOrders.text = "${item.totalOrders} đơn"
            holder.completedOrders.text = "${item.completedOrders} đơn"
        }

        override fun getItemCount() = items.size
    }
}
