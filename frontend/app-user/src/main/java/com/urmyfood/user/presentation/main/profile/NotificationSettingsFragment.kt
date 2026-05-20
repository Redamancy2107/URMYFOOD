package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.databinding.FragmentNotificationSettingsBinding
import com.urmyfood.user.di.ServiceLocator

class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationSettingsViewModel by viewModels {
        ServiceLocator.provideNotificationSettingsViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.switchOrders.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setOrdersEnabled(isChecked)
        }

        binding.switchPromotions.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPromotionsEnabled(isChecked)
        }

        binding.switchMessages.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMessagesEnabled(isChecked)
        }

        binding.switchSystem.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSystemEnabled(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.ordersEnabled.observe(viewLifecycleOwner) { enabled ->
            if (binding.switchOrders.isChecked != enabled) {
                binding.switchOrders.isChecked = enabled
            }
        }

        viewModel.promotionsEnabled.observe(viewLifecycleOwner) { enabled ->
            if (binding.switchPromotions.isChecked != enabled) {
                binding.switchPromotions.isChecked = enabled
            }
        }

        viewModel.messagesEnabled.observe(viewLifecycleOwner) { enabled ->
            if (binding.switchMessages.isChecked != enabled) {
                binding.switchMessages.isChecked = enabled
            }
        }

        viewModel.systemEnabled.observe(viewLifecycleOwner) { enabled ->
            if (binding.switchSystem.isChecked != enabled) {
                binding.switchSystem.isChecked = enabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
