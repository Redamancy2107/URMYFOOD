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
import com.urmyfood.user.databinding.FragmentOrderHistoryBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Màn hình Đơn hàng của tôi.
 * 4 tab lọc trạng thái + danh sách đơn hàng mẫu.
 */
class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private var selectedTab = 0
    private val viewModel: OrderHistoryViewModel by viewModels {
        ServiceLocator.provideOrderHistoryViewModelFactory()
    }
    private var orders: List<Order> = emptyList()

    data class Order(
        val orderId: String,
        val shop: String,
        val desc: String,
        val price: String,
        val date: String,
        val status: Int,
        val originalStatus: String,
        val rawCreatedAt: String,
        val imageUrl: String? = null
    )

    // Orders list is loaded from the backend through OrderHistoryViewModel.

    private val tabTitles by lazy {
        listOf(
            getString(R.string.order_tab_processing),
            getString(R.string.order_tab_delivering),
            getString(R.string.order_tab_delivered),
            getString(R.string.order_tab_cancelled)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        buildTabs()
        setupSwipeRefresh()
        observeOrders()
        viewModel.loadOrders()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadOrders()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary)
    }

    private fun buildTabs() {
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }
        binding.tabContainer.removeAllViews()

        tabTitles.forEachIndexed { i, title ->
            val tab = TextView(ctx).apply {
                text = title
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(16), dp(10), dp(16), dp(10))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
                setOnClickListener {
                    selectedTab = i
                    buildTabs()
                    renderOrders()
                }
            }
            if (i == selectedTab) {
                tab.setBackgroundResource(R.drawable.bg_btn_primary)
                tab.setTextColor(Color.WHITE)
            } else {
                tab.setBackgroundResource(R.drawable.bg_chip)
                tab.setTextColor(ctx.getColor(R.color.text_secondary))
            }
            binding.tabContainer.addView(tab)
        }
    }

    private fun renderOrders() {
        val ctx = requireContext()
        val dp = { v: Int -> (v * ctx.resources.displayMetrics.density).toInt() }
        binding.orderContainer.removeAllViews()

        val filtered = orders.filter { it.status == selectedTab }
        if (filtered.isEmpty()) {
            binding.orderContainer.addView(TextView(ctx).apply {
                text = "Không có đơn hàng nào"
                textSize = 15f
                setTextColor(ctx.getColor(R.color.text_hint))
                gravity = Gravity.CENTER
                setPadding(0, dp(48), 0, 0)
            })
            return
        }

        filtered.forEach { order ->
            val card = CardView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(12)
                }
                radius = dp(16).toFloat()
                cardElevation = dp(4).toFloat()
                setCardBackgroundColor(Color.WHITE)
            }

            val content = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(16), dp(20), dp(16))
            }

            // Date/Time at the top-left of the card
            content.addView(TextView(ctx).apply {
                text = order.date
                textSize = 12f
                setTextColor(ctx.getColor(R.color.text_hint))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }
            })

            // Horizontal layout under the Date/Time
            val bodyRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Left: Rounded CardView (radius 12dp, size 80dp x 80dp)
            val imgCard = CardView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(80), dp(80))
                radius = dp(12).toFloat()
                cardElevation = 0f
            }
            val imgView = android.widget.ImageView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
            com.bumptech.glide.Glide.with(ctx)
                .load(order.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.bg_food_banner)
                .into(imgView)
            imgCard.addView(imgView)
            bodyRow.addView(imgCard)

            // Right: Details column containing Shop Name, Description, and Price
            val detailsCol = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(12)
                }
            }

            // Shop Name (bold, 16sp, text_primary)
            detailsCol.addView(TextView(ctx).apply {
                text = order.shop
                textSize = 16f
                setTextColor(ctx.getColor(R.color.text_primary))
                setTypeface(typeface, Typeface.BOLD)
            })

            // Dish name x quantity (14sp, text_secondary)
            detailsCol.addView(TextView(ctx).apply {
                text = order.desc
                textSize = 14f
                setTextColor(ctx.getColor(R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                }
            })

            // Price (bold, 16sp, primary)
            detailsCol.addView(TextView(ctx).apply {
                text = order.price
                textSize = 16f
                setTextColor(ctx.getColor(R.color.primary))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            })

            bodyRow.addView(detailsCol)
            content.addView(bodyRow)

            // Action buttons for "Đã giao" tab
            if (selectedTab == 2) {
                val btnRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END
                    setPadding(0, dp(12), 0, 0)
                }

                val btnReview = TextView(ctx).apply {
                    text = ctx.getString(R.string.order_btn_review)
                    textSize = 14f
                    setTextColor(ctx.getColor(R.color.text_primary))
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(20), dp(10), dp(20), dp(10))
                    setBackgroundResource(R.drawable.bg_btn_outlined)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        marginEnd = dp(8)
                    }
                    setOnClickListener { Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show() }
                }
                btnRow.addView(btnReview)

                val btnReorder = TextView(ctx).apply {
                    text = ctx.getString(R.string.order_btn_reorder)
                    textSize = 14f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(20), dp(10), dp(20), dp(10))
                    setBackgroundResource(R.drawable.bg_btn_primary)
                    setOnClickListener { Toast.makeText(ctx, ctx.getString(R.string.toast_feature_in_development), Toast.LENGTH_SHORT).show() }
                }
                btnRow.addView(btnReorder)
                content.addView(btnRow)
            } else if (selectedTab == 0 && order.originalStatus == "PENDING") {
                try {
                    val createdAt = java.time.OffsetDateTime.parse(order.rawCreatedAt)
                    val now = java.time.OffsetDateTime.now()
                    val diffMinutes = java.time.Duration.between(createdAt, now).toMinutes()
                    if (diffMinutes < 5) {
                        val btnRow = LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.END
                            setPadding(0, dp(12), 0, 0)
                        }

                        val btnCancel = TextView(ctx).apply {
                            text = "Hủy đơn"
                            textSize = 14f
                            setTextColor(Color.WHITE)
                            setTypeface(typeface, Typeface.BOLD)
                            setPadding(dp(20), dp(10), dp(20), dp(10))
                            setBackgroundResource(R.drawable.bg_btn_primary)
                            setOnClickListener {
                                showCancelDialog(order.orderId)
                            }
                        }
                        btnRow.addView(btnCancel)
                        content.addView(btnRow)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            card.addView(content)
            binding.orderContainer.addView(card)
        }
    }

    private fun observeOrders() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            state.message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
            orders = state.orders
            renderOrders()
            
            // Stop refresh animation when loading completes
            binding.swipeRefreshLayout.isRefreshing = state.isLoading
        }
    }

    private fun showCancelDialog(orderId: String) {
        val ctx = requireContext()
        val input = android.widget.EditText(ctx).apply {
            hint = "Nhập lý do hủy"
            setPadding(32, 32, 32, 32)
        }
        val dialog = android.app.AlertDialog.Builder(ctx)
            .setTitle("Hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
            .setView(input)
            .setPositiveButton("Hủy đơn", null)
            .setNegativeButton("Đóng", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(ctx, "Vui lòng nhập lý do", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.cancelOrder(orderId, reason)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
