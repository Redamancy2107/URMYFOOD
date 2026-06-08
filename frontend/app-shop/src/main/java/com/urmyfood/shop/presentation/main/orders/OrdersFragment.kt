package com.urmyfood.shop.presentation.main.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainOrdersBinding
import com.urmyfood.shop.presentation.common.safeNavigate
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel.OrderStatus
import com.urmyfood.shop.presentation.main.orders.adapter.OrdersAdapter

class OrdersFragment : Fragment() {

    private var _binding: FragmentMainOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdersViewModel by viewModels()

    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupTabs() {
        val statuses = listOf(OrderStatus.WAITING, OrderStatus.PREPARING, OrderStatus.READY)
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }
        binding.tabContainer.removeAllViews()

        val selectedStatus = viewModel.selectedStatus.value ?: OrderStatus.WAITING

        statuses.forEach { status ->
            val tab = android.widget.TextView(ctx).apply {
                text = status.label
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(dp(16), dp(10), dp(16), dp(10))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
                setOnClickListener {
                    viewModel.setStatusFilter(status)
                }
            }
            if (status == selectedStatus) {
                tab.setBackgroundResource(R.drawable.bg_btn_primary)
                tab.setTextColor(android.graphics.Color.WHITE)
            } else {
                tab.setBackgroundResource(R.drawable.bg_chip)
                tab.setTextColor(ctx.getColor(R.color.text_secondary))
            }
            binding.tabContainer.addView(tab)
        }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(
            onActionClick = { order ->
                showActionConfirmation(order.orderId, order.status) {
                    viewModel.advanceOrderStatus(order.orderId)
                }
            },
            onItemClick = { order ->
                val args = Bundle().apply {
                    putString("orderId", order.orderId)
                }
                findNavController().safeNavigate(
                    R.id.action_orders_to_orderDetail,
                    args
                )
            }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = ordersAdapter
    }

    private fun showActionConfirmation(
        orderId: String,
        status: OrderStatus,
        onConfirm: () -> Unit
    ) {
        val title = if (status == OrderStatus.WAITING) {
            "Nhận chuẩn bị đơn"
        } else {
            "Hoàn thành chuẩn bị"
        }
        val message = if (status == OrderStatus.WAITING) {
            "Bạn muốn bắt đầu chuẩn bị cho đơn hàng $orderId?"
        } else {
            "Xác nhận đơn hàng $orderId đã chuẩn bị xong và sẵn sàng giao?"
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Xác nhận") { _, _ -> onConfirm() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.allOrders.observe(viewLifecycleOwner) { allOrders ->
            val status = viewModel.selectedStatus.value ?: OrderStatus.WAITING
            updateFilteredList(allOrders, status)
        }

        viewModel.selectedStatus.observe(viewLifecycleOwner) { status ->
            val allOrders = viewModel.allOrders.value.orEmpty()
            updateFilteredList(allOrders, status)
            setupTabs() // Re-render tabs to update styles
        }
    }

    private fun updateFilteredList(orders: List<OrdersViewModel.ShopOrder>, status: OrderStatus) {
        val filtered = orders.filter { it.status == status }
        ordersAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.rvOrders.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvOrders.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
