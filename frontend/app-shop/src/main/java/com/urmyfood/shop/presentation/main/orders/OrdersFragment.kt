package com.urmyfood.shop.presentation.main.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMainOrdersBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.presentation.common.safeNavigate
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel.StatusFilter
import com.urmyfood.shop.presentation.main.orders.adapter.OrdersAdapter

class OrdersFragment : Fragment() {

    private var _binding: FragmentMainOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdersViewModel by viewModels {
        ServiceLocator.provideOrdersViewModelFactory()
    }

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
        setupSwipeRefresh()
        observeViewModel()
        viewModel.loadOrders()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadOrders()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary)
    }

    private fun setupTabs() {
        val filters = StatusFilter.values()
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }
        binding.tabContainer.removeAllViews()

        val selectedFilter = viewModel.selectedFilter.value ?: StatusFilter.PENDING

        filters.forEach { filter ->
            val tab = android.widget.TextView(ctx).apply {
                text = filter.label
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(dp(16), dp(10), dp(16), dp(10))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
                setOnClickListener {
                    viewModel.setStatusFilter(filter)
                }
            }
            if (filter == selectedFilter) {
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
                showActionConfirmation(order) {
                    viewModel.advanceOrderStatus(order.orderId)
                }
            },
            onAcceptClick = { order ->
                viewModel.acceptOrder(order.orderId)
            },
            onRejectClick = { order ->
                showRejectDialog(order.orderId)
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
        binding.rvOrders.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = ordersAdapter
    }

    private fun showRejectDialog(orderId: String) {
        val input = EditText(requireContext()).apply {
            hint = "Nhập lý do từ chối..."
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Từ chối đơn hàng")
            .setView(input)
            .setPositiveButton("Xác nhận từ chối") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isBlank()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.rejectOrder(orderId, reason)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showActionConfirmation(order: Order, onConfirm: () -> Unit) {
        val nextLabel = when (order.orderStatus) {
            "ACCEPTED" -> "Bắt đầu lấy hàng"
            "PICKING_UP" -> "Bắt đầu giao"
            "DELIVERING" -> "Hoàn thành giao hàng"
            else -> return
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(nextLabel)
            .setMessage("Xác nhận chuyển trạng thái đơn hàng?")
            .setPositiveButton("Xác nhận") { _, _ -> onConfirm() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.allOrders.observe(viewLifecycleOwner) { allOrders ->
            val filter = viewModel.selectedFilter.value ?: StatusFilter.PENDING
            updateFilteredList(allOrders, filter)
        }

        viewModel.selectedFilter.observe(viewLifecycleOwner) { filter ->
            val allOrders = viewModel.allOrders.value.orEmpty()
            updateFilteredList(allOrders, filter)
            setupTabs()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (!loading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            if (loading && !binding.swipeRefreshLayout.isRefreshing) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFilteredList(orders: List<Order>, filter: StatusFilter) {
        val filtered = orders.filter { order ->
            when (filter) {
                StatusFilter.PENDING -> order.orderStatus == "PENDING"
                StatusFilter.PROCESSING -> order.orderStatus == "ACCEPTED" || order.orderStatus == "PICKING_UP"
                StatusFilter.DELIVERING -> order.orderStatus == "DELIVERING"
                StatusFilter.COMPLETED -> order.orderStatus == "COMPLETED"
                StatusFilter.CANCELLED -> order.orderStatus in listOf("CANCELLED", "REJECTED", "EXPIRED")
            }
        }
        ordersAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.swipeRefreshLayout.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.swipeRefreshLayout.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
