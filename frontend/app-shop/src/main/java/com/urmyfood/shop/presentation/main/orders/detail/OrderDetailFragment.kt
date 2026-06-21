package com.urmyfood.shop.presentation.main.orders.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentOrderDetailBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.presentation.main.orders.detail.adapter.DishDetailAdapter
import java.text.NumberFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderDetailViewModel by viewModels {
        ServiceLocator.provideOrderDetailViewModelFactory()
    }

    private lateinit var adapter: DishDetailAdapter
    private var currentOrderId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentOrderId = arguments?.getString("orderId") ?: ""

        setupToolbar(currentOrderId)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadOrderDetail(currentOrderId)
    }

    private fun setupToolbar(orderId: String) {
        binding.toolbar.title = "Đơn hàng ${orderId.take(8).uppercase()}"
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
        viewModel.orderDetail.observe(viewLifecycleOwner) { order ->
            if (order != null) bindOrder(order)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.actionSuccess.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindOrder(order: Order) {
        binding.toolbar.subtitle = order.createdAt.take(16).replace("T", " ")
        binding.tvCustomerName.text = order.customerName
        val baseStatus = when (order.orderStatus) {
            "PENDING" -> "Chờ xác nhận"
            "ACCEPTED" -> "Đã xác nhận"
            "PICKING_UP" -> "Đang lấy hàng"
            "DELIVERING" -> "Đang giao hàng"
            "COMPLETED" -> "Hoàn thành"
            "CANCELLED" -> "Đã hủy"
            "REJECTED" -> "Đã từ chối"
            "EXPIRED" -> "Hết hạn"
            else -> order.orderStatus
        }
        val paymentLabel = if (order.paymentStatus == "PAID") " (Đã thanh toán)" else ""
        binding.tvOrderStatus.text = baseStatus + paymentLabel

        if (order.note.isNullOrBlank()) {
            binding.llBuyerNote.visibility = View.GONE
        } else {
            binding.llBuyerNote.visibility = View.VISIBLE
            binding.tvBuyerNote.text = order.note
        }

        binding.ivCustomerAvatar.setImageResource(R.drawable.ic_person)

        binding.tvSubtotal.text = formatCurrency(order.totalAmount.toLong())
        binding.tvPlatformFee.text = formatCurrency(order.discountAmount.toLong())
        binding.tvEarnings.text = formatCurrency(order.finalAmount.toLong())

        val dishItems = order.items.map { item ->
            com.urmyfood.shop.presentation.main.orders.detail.adapter.DishDetailAdapter.DishItem(
                name = item.dishNameSnapshot,
                quantity = item.quantity,
                price = item.priceAtPurchase.toLong(),
                imageUrl = item.imageUrlSnapshot
            )
        }
        adapter.submitList(dishItems)

        setupActionButton(order)
    }

    private fun setupActionButton(order: Order) {
        when (order.orderStatus) {
            "PENDING" -> {
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = "Xác nhận đơn hàng"
                binding.btnAction.setOnClickListener {
                    viewModel.updateStatus(order.orderId, "ACCEPTED")
                }
            }
            "ACCEPTED" -> {
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = "Bắt đầu lấy hàng"
                binding.btnAction.setOnClickListener {
                    viewModel.updateStatus(order.orderId, "PICKING_UP")
                }
            }
            "PICKING_UP" -> {
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = "Bắt đầu giao"
                binding.btnAction.setOnClickListener {
                    viewModel.updateStatus(order.orderId, "DELIVERING")
                }
            }
            "DELIVERING" -> {
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = "Hoàn thành giao hàng"
                binding.btnAction.setOnClickListener {
                    viewModel.updateStatus(order.orderId, "COMPLETED")
                }
            }
            else -> {
                binding.btnAction.visibility = View.GONE
            }
        }
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
                    viewModel.updateStatus(orderId, "REJECTED", reason)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
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
