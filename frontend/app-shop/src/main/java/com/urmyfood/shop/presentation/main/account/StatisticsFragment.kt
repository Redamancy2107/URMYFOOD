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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupToggleGroup() {
        // Handle pre-check states to ensure they match viewmodel defaults
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnDay -> viewModel.switchPeriod(StatisticsViewModel.Period.DAY)
                    R.id.btnMonth -> viewModel.switchPeriod(StatisticsViewModel.Period.MONTH)
                    R.id.btnYear -> viewModel.switchPeriod(StatisticsViewModel.Period.YEAR)
                    R.id.btnAll -> viewModel.switchPeriod(StatisticsViewModel.Period.ALL)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnDateSelector.setOnClickListener {
            val period = viewModel.selectedPeriod.value ?: return@setOnClickListener
            when (period) {
                StatisticsViewModel.Period.DAY -> {
                    showDatePicker()
                }
                StatisticsViewModel.Period.MONTH -> {
                    showMonthScrollPicker()
                }
                StatisticsViewModel.Period.YEAR -> {
                    showYearScrollPicker()
                }
                StatisticsViewModel.Period.ALL -> Unit
            }
        }
    }

    private fun showDatePicker() {
        val context = requireContext()
        val currentText = viewModel.selectorText.value ?: ""
        var currentDay = 8
        var currentMonth = 6
        var currentYear = 2026

        val parts = currentText.split("/")
        if (parts.size == 3) {
            try {
                currentDay = parts[0].toInt()
                currentMonth = parts[1].toInt()
                currentYear = parts[2].toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val dayPicker = android.widget.NumberPicker(context)
        val monthPicker = android.widget.NumberPicker(context)
        val yearPicker = android.widget.NumberPicker(context)

        fun updateDayRange() {
            val m = monthPicker.value
            val y = yearPicker.value
            val maxDays = when (m) {
                2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }

            val oldVal = dayPicker.value
            dayPicker.displayedValues = null
            dayPicker.minValue = 1
            dayPicker.maxValue = maxDays
            dayPicker.value = oldVal.coerceAtMost(maxDays)
            dayPicker.displayedValues = Array(maxDays) { String.format(Locale.getDefault(), "Ngày %02d", it + 1) }
        }

        yearPicker.apply {
            minValue = 2020
            maxValue = 2035
            value = currentYear
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnValueChangedListener { _, _, _ -> updateDayRange() }
        }

        monthPicker.apply {
            minValue = 1
            maxValue = 12
            value = currentMonth
            displayedValues = Array(12) { String.format(Locale.getDefault(), "Tháng %02d", it + 1) }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (8 * resources.displayMetrics.density).toInt()
            }
            setOnValueChangedListener { _, _, _ -> updateDayRange() }
        }

        dayPicker.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (8 * resources.displayMetrics.density).toInt()
            }
        }

        dayPicker.minValue = 1
        dayPicker.maxValue = 31
        updateDayRange()
        dayPicker.value = currentDay.coerceAtMost(dayPicker.maxValue)

        container.addView(dayPicker)
        container.addView(monthPicker)
        container.addView(yearPicker)

        MaterialAlertDialogBuilder(context)
            .setTitle("Chọn ngày / tháng / năm")
            .setView(container)
            .setPositiveButton("Xác nhận") { _, _ ->
                val selectedDateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayPicker.value, monthPicker.value, yearPicker.value)
                viewModel.selectDay(selectedDateStr)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showMonthScrollPicker() {
        val context = requireContext()
        val currentText = viewModel.selectorText.value ?: ""
        var currentMonth = 6
        var currentYear = 2026

        if (currentText.startsWith("Tháng ") && currentText.contains("/")) {
            try {
                val parts = currentText.substring(6).split("/")
                if (parts.size == 2) {
                    currentMonth = parts[0].toInt()
                    currentYear = parts[1].toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val monthPicker = android.widget.NumberPicker(context).apply {
            minValue = 1
            maxValue = 12
            value = currentMonth
            displayedValues = Array(12) { String.format(Locale.getDefault(), "Tháng %02d", it + 1) }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (8 * resources.displayMetrics.density).toInt()
            }
        }

        val yearPicker = android.widget.NumberPicker(context).apply {
            minValue = 2020
            maxValue = 2035
            value = currentYear
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        container.addView(monthPicker)
        container.addView(yearPicker)

        MaterialAlertDialogBuilder(context)
            .setTitle("Chọn tháng / năm")
            .setView(container)
            .setPositiveButton("Xác nhận") { _, _ ->
                val selectedMonthStr = String.format(Locale.getDefault(), "Tháng %02d/%d", monthPicker.value, yearPicker.value)
                viewModel.selectMonth(selectedMonthStr)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showYearScrollPicker() {
        val context = requireContext()
        val currentText = viewModel.selectorText.value ?: ""
        var currentYear = 2026

        if (currentText.startsWith("Năm ")) {
            try {
                currentYear = currentText.substring(4).toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val yearPicker = android.widget.NumberPicker(context).apply {
            minValue = 2020
            maxValue = 2035
            value = currentYear
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        container.addView(yearPicker)

        MaterialAlertDialogBuilder(context)
            .setTitle("Chọn năm")
            .setView(container)
            .setPositiveButton("Xác nhận") { _, _ ->
                val selectedYearStr = String.format(Locale.getDefault(), "Năm %d", yearPicker.value)
                viewModel.selectYear(selectedYearStr)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.totalRevenue.observe(viewLifecycleOwner) { total ->
            binding.tvRevenueValue.text = formatCurrency(total)
        }

        viewModel.totalOrders.observe(viewLifecycleOwner) { orders ->
            binding.tvOrdersValue.text = orders.toString()
        }

        viewModel.cancelledOrders.observe(viewLifecycleOwner) { cancelled ->
            binding.tvCancelledValue.text = cancelled.toString()
        }

        viewModel.cancellationRate.observe(viewLifecycleOwner) { rate ->
            binding.tvRateValue.text = String.format(Locale.getDefault(), "%.1f%%", rate)
        }

        viewModel.selectorText.observe(viewLifecycleOwner) { text ->
            binding.btnDateSelector.text = text
        }

        viewModel.showSelector.observe(viewLifecycleOwner) { show ->
            binding.btnDateSelector.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun formatCurrency(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
