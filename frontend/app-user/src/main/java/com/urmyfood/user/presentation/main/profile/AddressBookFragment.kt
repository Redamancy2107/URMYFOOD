package com.urmyfood.user.presentation.main.profile

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAddressBookBinding

/**
 * Màn hình Sổ địa chỉ.
 * Hiển thị danh sách địa chỉ mẫu, hỗ trợ đổi mặc định, sửa/xóa.
 */
class AddressBookFragment : Fragment() {

    private var _binding: FragmentAddressBookBinding? = null
    private val binding get() = _binding!!

    data class Address(var label: String, var name: String, var phone: String, var detail: String, var isDefault: Boolean)

    companion object {
        val addresses = mutableListOf(
            Address(
                label = "Nhà riêng",
                name = "Nguyễn Lê Vân Anh",
                phone = "0912 345 678",
                detail = "123 Nguyễn Văn Linh, Quận 7, TP.HCM",
                isDefault = true
            ),
            Address(
                label = "Công ty",
                name = "Nguyễn Lê Vân Anh",
                phone = "0912 345 678",
                detail = "Tòa nhà Bitexco, 2 Hải Triều, Quận 1, TP.HCM",
                isDefault = false
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddressBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnAddAddress.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("address_index", -1)
            }
            findNavController().navigate(R.id.addressEditFragment, bundle)
        }
        renderAddresses()
    }

    private fun renderAddresses() {
        binding.addressContainer.removeAllViews()
        addresses.forEachIndexed { index, addr ->
            binding.addressContainer.addView(createAddressCard(addr, index))
        }
    }

    private fun createAddressCard(addr: Address, index: Int): CardView {
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }

        val card = CardView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            }
            radius = dp(20).toFloat()
            cardElevation = dp(4).toFloat()
            setCardBackgroundColor(Color.WHITE)
            if (addr.isDefault) setContentPadding(0, 0, 0, 0)
        }

        val wrapper = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            if (addr.isDefault) {
                background = ctx.getDrawable(R.drawable.bg_btn_outlined)
            }
        }

        // Top row: label badge + action icons
        val topRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Label badge
        val badge = TextView(ctx).apply {
            text = addr.label
            textSize = 12f
            setTextColor(if (addr.isDefault) ctx.getColor(R.color.primary) else Color.WHITE)
            setPadding(dp(12), dp(4), dp(12), dp(4))
            background = ctx.getDrawable(if (addr.isDefault) R.drawable.bg_badge_red else R.drawable.bg_badge_solid_red)
        }
        topRow.addView(badge)

        if (addr.isDefault) {
            val defBadge = TextView(ctx).apply {
                text = ctx.getString(R.string.address_book_default)
                textSize = 12f
                setTextColor(ctx.getColor(R.color.primary))
                setPadding(dp(8), 0, 0, 0)
            }
            topRow.addView(defBadge)
        }

        // Spacer
        topRow.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        })

        // Edit button
        val btnEdit = ImageButton(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
            setImageResource(R.drawable.ic_edit)
            setColorFilter(ctx.getColor(R.color.text_secondary))
            setBackgroundResource(android.R.color.transparent)
            setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("address_index", index)
                }
                findNavController().navigate(R.id.addressEditFragment, bundle)
            }
        }
        topRow.addView(btnEdit)

        // Delete button
        val btnDelete = ImageButton(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
            setImageResource(R.drawable.ic_delete)
            setColorFilter(ctx.getColor(R.color.error))
            setBackgroundResource(android.R.color.transparent)
            setOnClickListener {
                addresses.removeAt(index)
                renderAddresses()
                Toast.makeText(ctx, ctx.getString(R.string.address_book_deleted), Toast.LENGTH_SHORT).show()
            }
        }
        topRow.addView(btnDelete)
        wrapper.addView(topRow)

        // Name
        wrapper.addView(TextView(ctx).apply {
            text = addr.name
            textSize = 16f
            setTextColor(ctx.getColor(R.color.text_primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12), 0, 0)
        })

        // Phone
        wrapper.addView(TextView(ctx).apply {
            text = addr.phone
            textSize = 14f
            setTextColor(ctx.getColor(R.color.text_secondary))
            setPadding(0, dp(4), 0, 0)
        })

        // Address detail
        wrapper.addView(TextView(ctx).apply {
            text = addr.detail
            textSize = 14f
            setTextColor(ctx.getColor(R.color.text_secondary))
            setPadding(0, dp(4), 0, 0)
        })

        card.addView(wrapper)

        // Click to set as default
        card.setOnClickListener {
            addresses.forEachIndexed { i, a -> a.isDefault = (i == index) }
            renderAddresses()
        }

        return card
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
