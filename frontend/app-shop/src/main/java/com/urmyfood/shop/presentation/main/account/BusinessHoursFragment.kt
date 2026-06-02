package com.urmyfood.shop.presentation.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentBusinessHoursBinding
import com.urmyfood.shop.presentation.main.account.hours.BusinessHoursViewModel
import com.urmyfood.shop.presentation.main.account.hours.adapter.BusinessDaysAdapter

class BusinessHoursFragment : Fragment() {

    private var _binding: FragmentBusinessHoursBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BusinessHoursViewModel by viewModels()

    private lateinit var adapter: BusinessDaysAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = BusinessDaysAdapter { day, isChecked ->
            viewModel.toggleDay(day.dayName, isChecked)
        }
        binding.rvBusinessDays.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBusinessDays.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.businessDays.observe(viewLifecycleOwner) { days ->
            adapter.submitList(days)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            // Mock action - remove when BE ready
            Toast.makeText(requireContext(), "Đã lưu thay đổi giờ mở cửa thành công", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
