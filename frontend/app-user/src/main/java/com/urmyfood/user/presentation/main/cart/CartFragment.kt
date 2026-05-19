package com.urmyfood.user.presentation.main.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.data.local.CartManager
import com.urmyfood.user.databinding.FragmentMainCartBinding
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

    private lateinit var cartManager: CartManager
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
        
        cartManager = CartManager(requireContext())
        setupRecyclerView()
        setupClickListeners()
        loadCartItems()
    }

    private fun setupRecyclerView() {
        binding.rvCartItems.adapter = cartAdapter

        cartAdapter.onQuantityChanged = { item, newQty ->
            cartManager.updateCartItemQuantity(item.postId, item.selectedOption, newQty)
            loadCartItems()
        }

        cartAdapter.onDeleteClicked = { item ->
            cartManager.removeCartItem(item.postId, item.selectedOption)
            loadCartItems()
        }
    }

    private fun setupClickListeners() {
        binding.btnExplore.setOnClickListener {
            // Quay về màn hình Trang chủ bằng cách gọi Jetpack Navigation
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnCheckout.setOnClickListener {
            val paymentSheet = PaymentMethodSheetFragment()
            paymentSheet.show(childFragmentManager, PaymentMethodSheetFragment.TAG)
        }
    }

    /**
     * Tải danh sách sản phẩm từ CartManager và cập nhật UI.
     */
    fun loadCartItems() {
        val items = cartManager.getCartItems()
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

            // Calculate total sum
            val total = items.sumOf { it.price * it.quantity }
            binding.tvTotalPayment.text = "${currencyFormat.format(total)}đ"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
