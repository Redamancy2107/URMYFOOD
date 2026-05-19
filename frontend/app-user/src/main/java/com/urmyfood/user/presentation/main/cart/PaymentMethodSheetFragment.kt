package com.urmyfood.user.presentation.main.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.R
import com.urmyfood.user.data.local.CartManager
import com.urmyfood.user.databinding.LayoutPaymentMethodSheetBinding
import com.urmyfood.user.presentation.main.profile.OrderHistoryFragment.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bottom Sheet cho phép chọn Phương thức thanh toán (Tiền mặt / Thẻ).
 * Xác nhận xong sẽ chuyển các thẻ món ăn sang màn hình Lịch sử đơn hàng và dọn sạch giỏ hàng.
 */
class PaymentMethodSheetFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutPaymentMethodSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartManager: CartManager
    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private var isPayCardSelected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutPaymentMethodSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cartManager = CartManager(requireContext())
        setupBottomSheetBehavior()
        setupSelectionLogic()
        setupClickListeners()
    }

    private fun setupBottomSheetBehavior() {
        val behavior = (dialog as? BottomSheetDialog)?.behavior
        behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
        }
    }

    private fun setupSelectionLogic() {
        with(binding) {
            // Click Cash Card
            cvPayCash.setOnClickListener {
                isPayCardSelected = false
                updateSelectionUi()
            }

            // Click Credit Card
            cvPayCard.setOnClickListener {
                isPayCardSelected = true
                updateSelectionUi()
            }
        }
    }

    private fun updateSelectionUi() {
        val ctx = requireContext()
        with(binding) {
            if (isPayCardSelected) {
                // Select Card
                cvPayCard.strokeColor = ctx.getColor(R.color.primary)
                ivCardIcon.imageTintList = android.content.res.ColorStateList.valueOf(ctx.getColor(R.color.primary))
                rbPayCard.isChecked = true

                // Unselect Cash
                cvPayCash.strokeColor = ctx.getColor(R.color.divider)
                ivCashIcon.imageTintList = android.content.res.ColorStateList.valueOf(ctx.getColor(R.color.text_secondary))
                rbPayCash.isChecked = false
            } else {
                // Select Cash
                cvPayCash.strokeColor = ctx.getColor(R.color.primary)
                ivCashIcon.imageTintList = android.content.res.ColorStateList.valueOf(ctx.getColor(R.color.primary))
                rbPayCash.isChecked = true

                // Unselect Card
                cvPayCard.strokeColor = ctx.getColor(R.color.divider)
                ivCardIcon.imageTintList = android.content.res.ColorStateList.valueOf(ctx.getColor(R.color.text_secondary))
                rbPayCard.isChecked = false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirmPayment.setOnClickListener {
            val cartItems = cartManager.getCartItems()
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show()
                dismiss()
                return@setOnClickListener
            }

            // Get current timestamp formatted nicely (e.g. 18/05/2026 20:30)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentDateStr = sdf.format(Date())

            // Loop and add to Lịch sử đơn hàng dynamically
            cartItems.forEach { item ->
                // Create new Order with status = 0 (Processing)
                val order = Order(
                    shop = item.shopName,
                    desc = "${item.dishName} x${item.quantity}",
                    price = "${currencyFormat.format(item.price * item.quantity)}đ",
                    date = currentDateStr,
                    status = 0, // Đang xử lý
                    imageUrl = item.imageUrl
                )
                cartManager.addOrder(order)
            }

            // Clear Cart
            cartManager.clearCart()

            // Trigger parent CartFragment to refresh its state if visible
            (parentFragment as? CartFragment)?.loadCartItems()

            Toast.makeText(
                requireContext(),
                "Đặt hàng thành công!",
                Toast.LENGTH_SHORT
            ).show()

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PaymentMethodSheetFragment"
    }
}
