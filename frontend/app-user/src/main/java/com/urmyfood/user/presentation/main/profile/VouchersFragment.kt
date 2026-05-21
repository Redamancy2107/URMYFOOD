package com.urmyfood.user.presentation.main.profile

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.databinding.FragmentVouchersBinding
import com.urmyfood.user.di.ServiceLocator
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Voucher của tôi.
 * Hiển thị danh sách voucher khả dụng từ API.
 */
class VouchersFragment : Fragment() {

    private var _binding: FragmentVouchersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VouchersViewModel by viewModels {
        ServiceLocator.provideVouchersViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVouchersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        observeViewModel()
        viewModel.loadVouchers()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VouchersUiState.Loading -> { /* Could show shimmer */ }
                is VouchersUiState.Success -> renderVouchers(state.vouchers)
                is VouchersUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is VouchersUiState.Idle -> Unit
            }
        }
    }

    private fun renderVouchers(vouchers: List<VoucherResponse>) {
        binding.voucherContainer.removeAllViews()
        val ctx = requireContext()

        if (vouchers.isEmpty()) {
            val emptyText = TextView(ctx).apply {
                text = "Hiện tại chưa có voucher nào khả dụng"
                textSize = 15f
                setTextColor(ctx.getColor(R.color.text_secondary))
                gravity = Gravity.CENTER
                setPadding(0, (48 * ctx.resources.displayMetrics.density).toInt(), 0, 0)
            }
            binding.voucherContainer.addView(emptyText)
            return
        }

        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }
        val primaryColor = ctx.getColor(R.color.primary)
        val currencyFormat = NumberFormat.getInstance(Locale("vi", "VN"))

        vouchers.forEach { v ->
            val card = CardView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
                radius = dp(16).toFloat()
                cardElevation = dp(4).toFloat()
                setCardBackgroundColor(Color.WHITE)
            }

            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Color accent bar
            row.addView(View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(6), LinearLayout.LayoutParams.MATCH_PARENT)
                setBackgroundColor(primaryColor)
            })

            // Info column
            val info = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(dp(16), dp(16), dp(8), dp(16))
            }
            info.addView(TextView(ctx).apply {
                text = v.code
                textSize = 14f
                setTextColor(primaryColor)
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.05f
            })
            info.addView(TextView(ctx).apply {
                text = v.title
                textSize = 16f
                setTextColor(ctx.getColor(R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(4), 0, 0)
            })
            info.addView(TextView(ctx).apply {
                text = v.description ?: "Giảm ${currencyFormat.format(v.discountValue.toLong())}đ"
                textSize = 13f
                setTextColor(ctx.getColor(R.color.text_secondary))
                setPadding(0, dp(2), 0, 0)
            })
            info.addView(TextView(ctx).apply {
                text = "HSD: ${v.expiryDate}"
                textSize = 12f
                setTextColor(ctx.getColor(R.color.text_hint))
                setPadding(0, dp(8), 0, 0)
            })
            row.addView(info)

            // Use button
            row.addView(TextView(ctx).apply {
                text = ctx.getString(R.string.voucher_use_now)
                textSize = 13f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setBackgroundResource(R.drawable.bg_btn_primary)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(16) }
                setOnClickListener {
                    Toast.makeText(ctx, "Đã sao chép mã: ${v.code}", Toast.LENGTH_SHORT).show()
                    val clipboard = ctx.getSystemService(android.content.ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("voucher_code", v.code))
                }
            })

            card.addView(row)
            binding.voucherContainer.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
