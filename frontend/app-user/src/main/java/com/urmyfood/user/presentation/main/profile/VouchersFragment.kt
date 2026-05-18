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
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentVouchersBinding

/**
 * Màn hình Voucher của tôi.
 * Hiển thị danh sách voucher khuyến mãi dạng thẻ bo góc.
 */
class VouchersFragment : Fragment() {

    private var _binding: FragmentVouchersBinding? = null
    private val binding get() = _binding!!

    data class Voucher(val code: String, val title: String, val discount: String, val expiry: String, val color: Int)

    private val vouchers = listOf(
        Voucher("FREESHIP50", "Miễn phí vận chuyển", "Giảm 50K phí ship", "HSD: 30/06/2026", R.color.primary),
        Voucher("FOOD30", "Giảm 30% đơn hàng", "Tối đa 100K", "HSD: 15/06/2026", R.color.primary),
        Voucher("NEWUSER", "Ưu đãi khách mới", "Giảm 25K cho đơn đầu tiên", "HSD: 31/07/2026", R.color.primary),
        Voucher("COMBO20", "Combo tiết kiệm", "Giảm 20K cho combo 2 món", "HSD: 20/06/2026", R.color.primary)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVouchersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        renderVouchers()
    }

    private fun renderVouchers() {
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }

        vouchers.forEach { v ->
            val resolvedColor = ctx.getColor(v.color)
            val card = CardView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(12)
                }
                radius = dp(16).toFloat()
                cardElevation = dp(4).toFloat()
                setCardBackgroundColor(Color.WHITE)
            }

            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Color accent bar
            val accent = View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(6), LinearLayout.LayoutParams.MATCH_PARENT)
                setBackgroundColor(resolvedColor)
            }
            row.addView(accent)

            // Info column
            val info = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(dp(16), dp(16), dp(8), dp(16))
            }
            info.addView(TextView(ctx).apply {
                text = v.code
                textSize = 14f
                setTextColor(resolvedColor)
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
                text = v.discount
                textSize = 13f
                setTextColor(ctx.getColor(R.color.text_secondary))
                setPadding(0, dp(2), 0, 0)
            })
            info.addView(TextView(ctx).apply {
                text = v.expiry
                textSize = 12f
                setTextColor(ctx.getColor(R.color.text_hint))
                setPadding(0, dp(8), 0, 0)
            })
            row.addView(info)

            // Use button
            val btn = TextView(ctx).apply {
                text = ctx.getString(R.string.voucher_use_now)
                textSize = 13f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setBackgroundResource(R.drawable.bg_btn_primary)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = dp(16)
                }
                setOnClickListener {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show()
                }
            }
            row.addView(btn)

            card.addView(row)
            binding.voucherContainer.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
