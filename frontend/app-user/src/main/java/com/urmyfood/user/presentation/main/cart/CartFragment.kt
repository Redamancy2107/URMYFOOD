package com.urmyfood.user.presentation.main.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainCartBinding
import com.urmyfood.user.di.ServiceLocator
import com.urmyfood.user.presentation.main.cart.adapter.CartAdapter
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Giỏ hàng (CartFragment).
 * Hiển thị danh sách món ăn, cho phép chỉnh số lượng inline,
 * hiển thị tổng tiền và mở Bottom Sheet thanh toán.
 */
class CartFragment : Fragment() {

    private var _binding: FragmentMainCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels {
        ServiceLocator.provideCartViewModelFactory()
    }
    private val cartAdapter = CartAdapter()
    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeCart()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        binding.rvCartItems.adapter = cartAdapter

        cartAdapter.onQuantityChanged = { item, newQty ->
            viewModel.updateQuantity(item, newQty)
        }

        cartAdapter.onDeleteClicked = { item ->
            viewModel.deleteItem(item)
        }
    }

    private fun setupClickListeners() {
        binding.btnExplore.setOnClickListener {
            // Quay về màn hình Trang chủ bằng cách gọi Jetpack Navigation
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnCheckout.setOnClickListener {
            val subtotal = (viewModel.uiState.value?.totalAmount ?: 0.0).toLong()
            val paymentSheet = PaymentMethodSheetFragment.newInstance(subtotal)
            paymentSheet.show(childFragmentManager, PaymentMethodSheetFragment.TAG)
        }
    }

    private fun observeCart() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            state.message?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
            renderCart(state)
        }
    }

    fun loadCartItems() {
        viewModel.loadCart()
    }

    private fun renderCart(state: CartUiState) {
        val items = state.items
        if (items.isEmpty()) {
            binding.rvCartItems.visibility = View.GONE
            binding.layoutEmptyCart.visibility = View.VISIBLE
            binding.tvTotalPayment.text = "0đ"
            binding.btnCheckout.isEnabled = false
            binding.btnCheckout.alpha = 0.5f
        } else {
            binding.rvCartItems.visibility = View.VISIBLE
            binding.layoutEmptyCart.visibility = View.GONE
            binding.btnCheckout.isEnabled = true
            binding.btnCheckout.alpha = 1.0f
            
            // Submit list to Adapter
            cartAdapter.submitList(items)

            binding.tvTotalPayment.text = "${currencyFormat.format(state.totalAmount)}đ"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
