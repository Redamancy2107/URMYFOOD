package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAddressEditBinding
import com.urmyfood.user.di.ServiceLocator

/**
 * Màn hình Thêm / Sửa địa chỉ.
 * Kết nối API backend để persist dữ liệu.
 */
class AddressEditFragment : Fragment() {

    private var _binding: FragmentAddressEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddressEditViewModel by viewModels {
        ServiceLocator.provideAddressEditViewModelFactory()
    }

    private var selectedLabel = "Nhà riêng"
    private var addressId: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddressEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argId = arguments?.getLong("address_id", -1L) ?: -1L
        addressId = if (argId > 0) argId else null

        setupListeners()
        observeViewModel()

        if (addressId != null) {
            binding.tvTitle.text = getString(R.string.address_edit_title_edit)
            viewModel.loadAddressForEdit(addressId!!)
        } else {
            binding.tvTitle.text = getString(R.string.address_edit_title_add)
            viewModel.loadAddressCount()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AddressEditUiState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Đang lưu..."
                }
                is AddressEditUiState.Loaded -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = getString(R.string.address_edit_btn_save)
                    val addr = state.address
                    binding.etFullName.setText(addr.name)
                    binding.etPhone.setText(addr.phone)
                    binding.etDetail.setText(addr.detail)
                    selectedLabel = addr.label
                    binding.switchDefault.isChecked = addr.isDefault
                    if (addr.isDefault) binding.switchDefault.isEnabled = false
                    updateLabelUI()
                }
                is AddressEditUiState.SaveSuccess -> {
                    Toast.makeText(
                        requireContext(),
                        if (addressId != null) getString(R.string.address_edit_success_update)
                        else getString(R.string.address_edit_success_add),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
                is AddressEditUiState.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = getString(R.string.address_edit_btn_save)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is AddressEditUiState.Idle -> {
                    // Set default toggle for new address
                    if (addressId == null && viewModel.isFirstAddress) {
                        binding.switchDefault.isChecked = true
                        binding.switchDefault.isEnabled = false
                    }
                    updateLabelUI()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnHome.setOnClickListener { selectedLabel = "Nhà riêng"; updateLabelUI() }
        binding.btnWork.setOnClickListener { selectedLabel = "Công ty"; updateLabelUI() }
        binding.btnSave.setOnClickListener { saveAddress() }
    }

    private fun updateLabelUI() {
        val ctx = requireContext()
        if (selectedLabel == "Nhà riêng") {
            binding.btnHome.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_solid_red)
            binding.btnHome.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            binding.btnWork.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_light)
            binding.btnWork.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
        } else {
            binding.btnHome.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_light)
            binding.btnHome.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            binding.btnWork.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_solid_red)
            binding.btnWork.setTextColor(ContextCompat.getColor(ctx, R.color.white))
        }
    }

    private fun saveAddress() {
        val name = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val detail = binding.etDetail.text.toString().trim()
        val isDefault = binding.switchDefault.isChecked

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.address_edit_error_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.address_edit_error_phone), Toast.LENGTH_SHORT).show()
            return
        }
        if (detail.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.address_edit_error_detail), Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveAddress(addressId, selectedLabel, name, phone, detail, isDefault)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
