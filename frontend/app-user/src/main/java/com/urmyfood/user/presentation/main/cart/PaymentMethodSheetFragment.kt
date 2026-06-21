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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        // Load stored addresses and vouchers
        viewModel.loadData()
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
        // Confirm Button
        binding.btnConfirmPayment.setOnClickListener {
            val paymentMethod = if (isPayCardSelected) "MOMO" else "COD"
            val deliveryAddress = binding.etDeliveryAddress.text?.toString()?.trim().orEmpty()
            val note = binding.etNote.text?.toString()?.trim()
            val voucherCode = binding.etVoucherCode.text?.toString()?.trim()

            viewModel.checkout(
                paymentMethod = paymentMethod,
                deliveryAddress = deliveryAddress,
                note = if (note.isNullOrBlank()) null else note,
                voucherCode = if (voucherCode.isNullOrBlank()) null else voucherCode
            )
        }

        // Saved Address Selection Button
        binding.btnSelectSavedAddress.setOnClickListener {
            val addresses = viewModel.addresses.value.orEmpty()
            if (addresses.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa có địa chỉ nào được lưu", Toast.LENGTH_SHORT).show()
            } else {
                val items = addresses.map { "[${it.label}] ${it.name} (${it.phone}): ${it.detail}" }.toTypedArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn địa chỉ giao hàng")
                    .setItems(items) { _, which ->
                        val selected = addresses[which]
                        viewModel.selectAddress(selected)
                    }
                    .show()
            }
        }

        // Voucher Selection Button
        binding.btnSelectSavedVoucher.setOnClickListener {
            val vouchers = viewModel.vouchers.value.orEmpty()
            if (vouchers.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa có mã ưu đãi nào khả dụng", Toast.LENGTH_SHORT).show()
            } else {
                val items = vouchers.map { "${it.code} - ${it.title} (Giảm ${it.discountValue.toInt()}đ)" }.toTypedArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn mã ưu đãi / Voucher")
                    .setItems(items) { _, which ->
                        val selected = vouchers[which]
                        viewModel.selectVoucher(selected)
                    }
                    .show()
            }
        }
    }

    private fun observeCheckout() {
        // Observe checkout state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.btnConfirmPayment.isEnabled = !state.isLoading
            state.message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                if (state.isSuccess) {
                    (parentFragment as? CartFragment)?.loadCartItems()
                    try {
                        if (state.paymentMethod == "MOMO" && !state.qrCode.isNullOrBlank()) {
                            val bundle = Bundle().apply {
                                putString("qrCode", state.qrCode)
                                putLong("amount", state.finalAmount)
                                putString("orderId", state.orderId)
                            }
                            findNavController().navigate(R.id.paymentQrFragment, bundle)
                        } else {
                            findNavController().navigate(R.id.orderHistoryFragment)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dismiss()
                }
                viewModel.clearMessage()
            }
        }

        // Observe selected address to pre-fill the EditText
        viewModel.selectedAddress.observe(viewLifecycleOwner) { address ->
            address?.let {
                binding.etDeliveryAddress.setText(it.detail)
            }
        }

        // Observe selected voucher to fill the EditText
        viewModel.selectedVoucher.observe(viewLifecycleOwner) { voucher ->
            voucher?.let {
                binding.etVoucherCode.setText(it.code)
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
