package com.urmyfood.shop.presentation.main.orders.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentOrderDetailBinding
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel.OrderStatus
import com.urmyfood.shop.presentation.main.orders.detail.adapter.DishDetailAdapter
import java.text.NumberFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderDetailViewModel by viewModels()

    private lateinit var adapter: DishDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val orderId = arguments?.getString("orderId") ?: "#ORD-1000"

        setupToolbar(orderId)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadOrderDetail(orderId)
    }

    private fun setupToolbar(orderId: String) {
        binding.toolbar.title = "Đơn hàng $orderId"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = DishDetailAdapter()
        binding.rvDishes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDishes.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.orderDetail.observe(viewLifecycleOwner) { detail ->
            binding.toolbar.subtitle = detail.timestamp
            binding.tvCustomerName.text = detail.customerName
            binding.tvOrderStatus.text = detail.status.label

            // Buyer Note
            if (detail.buyerNote.isNullOrBlank()) {
                binding.llBuyerNote.visibility = View.GONE
            } else {
                binding.llBuyerNote.visibility = View.VISIBLE
                binding.tvBuyerNote.text = detail.buyerNote
            }

            // Customer Avatar (Placeholder)
            binding.ivCustomerAvatar.setImageResource(R.drawable.ic_person)

            // Calculations
            val subtotal = viewModel.getSubtotal()
            val platformFee = viewModel.getPlatformFee()
            val earnings = viewModel.getEarnings()

            binding.tvSubtotal.text = formatCurrency(subtotal)
            binding.tvPlatformFee.text = formatCurrency(platformFee)
            binding.tvEarnings.text = formatCurrency(earnings)

            // Bind Dishes
            adapter.submitList(detail.dishes)

            // Bind Action Button
            setupActionButton(detail)
        }
    }

    private fun setupActionButton(detail: OrderDetailViewModel.OrderDetail) {
        when (detail.status) {
            OrderStatus.WAITING -> {
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = "Bắt đầu chuẩn bị"
                binding.btnAction.setOnClickListener {
                    viewModel.updateStatus(OrderStatus.PREPARING)
                    Toast.makeText(requireContext(), "Đã chuyển trạng thái sang chế biến", Toast.LENGTH_SHORT).show()
                }
            }
            OrderStatus.PREPARING -> {
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = "Đã sẵn sàng"
                binding.btnAction.setOnClickListener {
                    viewModel.updateStatus(OrderStatus.READY)
                    Toast.makeText(requireContext(), "Đơn hàng đã sẵn sàng giao khách", Toast.LENGTH_SHORT).show()
                }
            }
            OrderStatus.READY -> {
                binding.btnAction.visibility = View.GONE
            }
        }
    }

    private fun formatCurrency(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
