package com.urmyfood.user.presentation.main.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.R
import com.urmyfood.user.databinding.LayoutPaymentMethodSheetBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Bottom Sheet cho phép chọn Phương thức thanh toán (Tiền mặt / Thẻ).
 * Xác nhận xong sẽ chuyển các thẻ món ăn sang màn hình Lịch sử đơn hàng và dọn sạch giỏ hàng.
 */
class PaymentMethodSheetFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutPaymentMethodSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CheckoutViewModel by viewModels {
        ServiceLocator.provideCheckoutViewModelFactory()
    }
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
        
        setupBottomSheetBehavior()
        setupSelectionLogic()
        setupClickListeners()
        observeCheckout()
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
            val paymentMethod = if (isPayCardSelected) "MOMO" else "COD"
            viewModel.checkout(paymentMethod)
        }
    }

    private fun observeCheckout() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.btnConfirmPayment.isEnabled = !state.isLoading
            state.message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                if (state.isSuccess) {
                    (parentFragment as? CartFragment)?.loadCartItems()
                    dismiss()
                }
                viewModel.clearMessage()
            }
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
