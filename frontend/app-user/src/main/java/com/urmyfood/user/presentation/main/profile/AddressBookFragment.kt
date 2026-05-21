package com.urmyfood.user.presentation.main.profile

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.user.R
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.databinding.FragmentAddressBookBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Màn hình Sổ địa chỉ.
 * Hiển thị danh sách địa chỉ từ API, hỗ trợ đổi mặc định, sửa/xóa.
 */
class AddressBookFragment : Fragment() {

    private var _binding: FragmentAddressBookBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddressBookViewModel by viewModels {
        ServiceLocator.provideAddressBookViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddressBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnAddAddress.setOnClickListener {
            val bundle = Bundle().apply { putLong("address_id", -1L) }
            findNavController().navigate(R.id.addressEditFragment, bundle)
        }
        observeViewModel()
        viewModel.loadAddresses()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAddresses()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AddressBookUiState.Loading -> { /* Could show shimmer */ }
                is AddressBookUiState.Success -> renderAddresses(state.addresses)
                is AddressBookUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is AddressBookUiState.Idle -> Unit
            }
        }
        viewModel.actionMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearActionMessage()
            }
        }
    }

    private fun renderAddresses(addresses: List<AddressResponse>) {
        binding.addressContainer.removeAllViews()
        if (addresses.isEmpty()) {
            val ctx = requireContext()
            val emptyText = TextView(ctx).apply {
                text = "Bạn chưa có địa chỉ nào.\nHãy thêm địa chỉ giao hàng đầu tiên!"
                textSize = 15f
                setTextColor(ctx.getColor(R.color.text_secondary))
                gravity = Gravity.CENTER
                setPadding(0, (48 * ctx.resources.displayMetrics.density).toInt(), 0, 0)
            }
            binding.addressContainer.addView(emptyText)
            return
        }
        addresses.forEach { addr -> binding.addressContainer.addView(createAddressCard(addr)) }
    }

    private fun createAddressCard(addr: AddressResponse): CardView {
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }

        val card = CardView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
            radius = dp(20).toFloat()
            cardElevation = dp(4).toFloat()
            setCardBackgroundColor(Color.WHITE)
        }

        val wrapper = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            if (addr.isDefault) background = ctx.getDrawable(R.drawable.bg_btn_outlined)
        }

        // Top row: label badge + action icons
        val topRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val badge = TextView(ctx).apply {
            text = addr.label
            textSize = 12f
            setTextColor(if (addr.isDefault) ctx.getColor(R.color.primary) else Color.WHITE)
            setPadding(dp(12), dp(4), dp(12), dp(4))
            background = ctx.getDrawable(
                if (addr.isDefault) R.drawable.bg_badge_red else R.drawable.bg_badge_solid_red
            )
        }
        topRow.addView(badge)

        if (addr.isDefault) {
            topRow.addView(TextView(ctx).apply {
                text = ctx.getString(R.string.address_book_default)
                textSize = 12f
                setTextColor(ctx.getColor(R.color.primary))
                setPadding(dp(8), 0, 0, 0)
            })
        }

        topRow.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        })

        // Edit button
        topRow.addView(ImageButton(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
            setImageResource(R.drawable.ic_edit)
            setColorFilter(ctx.getColor(R.color.text_secondary))
            setBackgroundResource(android.R.color.transparent)
            setOnClickListener {
                val bundle = Bundle().apply { putLong("address_id", addr.id) }
                findNavController().navigate(R.id.addressEditFragment, bundle)
            }
        })

        // Delete button
        topRow.addView(ImageButton(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
            setImageResource(R.drawable.ic_delete)
            setColorFilter(ctx.getColor(R.color.error))
            setBackgroundResource(android.R.color.transparent)
            setOnClickListener { confirmDelete(addr) }
        })

        wrapper.addView(topRow)

        // Name
        wrapper.addView(TextView(ctx).apply {
            text = addr.name
            textSize = 16f
            setTextColor(ctx.getColor(R.color.text_primary))
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(12), 0, 0)
        })

        // Phone
        wrapper.addView(TextView(ctx).apply {
            text = addr.phone
            textSize = 14f
            setTextColor(ctx.getColor(R.color.text_secondary))
            setPadding(0, dp(4), 0, 0)
        })

        // Detail
        wrapper.addView(TextView(ctx).apply {
            text = addr.detail
            textSize = 14f
            setTextColor(ctx.getColor(R.color.text_secondary))
            setPadding(0, dp(4), 0, 0)
        })

        card.addView(wrapper)

        // Click to set as default
        card.setOnClickListener {
            if (!addr.isDefault) viewModel.setDefault(addr.id)
        }

        return card
    }

    private fun confirmDelete(addr: AddressResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa địa chỉ")
            .setMessage("Bạn có chắc muốn xóa \"${addr.label} - ${addr.name}\" không?")
            .setPositiveButton("Xóa") { _, _ -> viewModel.deleteAddress(addr.id) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
