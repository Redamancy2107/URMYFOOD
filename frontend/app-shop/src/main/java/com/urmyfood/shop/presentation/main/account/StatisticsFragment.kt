package com.urmyfood.shop.presentation.main.account

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentStatisticsBinding
import com.urmyfood.shop.presentation.main.account.stats.StatisticsViewModel
import java.text.NumberFormat
import java.util.Locale

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupToggleGroup()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupToggleGroup() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMonthly -> viewModel.switchPeriod(StatisticsViewModel.Period.MONTHLY)
                    R.id.btnQuarterly -> viewModel.switchPeriod(StatisticsViewModel.Period.QUARTERLY)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.totalRevenue.observe(viewLifecycleOwner) { total ->
            binding.tvTotalRevenue.text = formatCurrency(total)
        }

        viewModel.totalOrders.observe(viewLifecycleOwner) { orders ->
            binding.tvTotalOrders.text = getString(R.string.statistics_total_orders, orders)
        }

        viewModel.revenueEntries.observe(viewLifecycleOwner) { entries ->
            populateChart(entries)
        }
    }

    private fun populateChart(entries: List<StatisticsViewModel.RevenueEntry>) {
        binding.chartContainer.removeAllViews()
        val maxAmount = entries.maxOfOrNull { it.amount } ?: 1L

        val context = requireContext()
        val density = resources.displayMetrics.density
        val dpToPx = { dp: Int -> (dp * density).toInt() }

        for (entry in entries) {
            // Horizontal row layout
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
                gravity = Gravity.CENTER_VERTICAL
            }

            // Month/Period label
            val tvLabel = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(40), LinearLayout.LayoutParams.WRAP_CONTENT)
                text = entry.label
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }

            // Chart bar
            val barWidthPercentage = entry.amount.toFloat() / maxAmount
            val maxBarWidthDp = 180 // max DP width for the bar
            val barWidthPx = (maxBarWidthDp * barWidthPercentage * density).toInt().coerceAtLeast(dpToPx(8))

            val vBar = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(barWidthPx, dpToPx(16)).apply {
                    setMargins(dpToPx(8), 0, dpToPx(8), 0)
                }
                background = ContextCompat.getDrawable(context, R.drawable.bg_bar_chart)
            }

            // Amount text
            val tvAmount = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                }
                text = formatCurrency(entry.amount)
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                gravity = Gravity.END
            }

            rowLayout.addView(tvLabel)
            rowLayout.addView(vBar)
            rowLayout.addView(tvAmount)
            binding.chartContainer.addView(rowLayout)
        }
    }

    private fun formatCurrency(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
