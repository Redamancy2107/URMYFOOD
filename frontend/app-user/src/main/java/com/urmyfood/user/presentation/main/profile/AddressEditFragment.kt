package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentAddressEditBinding

class AddressEditFragment : Fragment() {

    private var _binding: FragmentAddressEditBinding? = null
    private val binding get() = _binding!!

    private var selectedLabel = "Nhà riêng"
    private var addressIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressIndex = arguments?.getInt("address_index", -1) ?: -1

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val hasNoAddress = AddressBookFragment.addresses.isEmpty()

        if (addressIndex != -1) {
            binding.tvTitle.text = getString(R.string.address_edit_title_edit)
            if (addressIndex in AddressBookFragment.addresses.indices) {
                val address = AddressBookFragment.addresses[addressIndex]
                binding.etFullName.setText(address.name)
                binding.etPhone.setText(address.phone)
                binding.etDetail.setText(address.detail)
                selectedLabel = address.label
                binding.switchDefault.isChecked = address.isDefault
                
                // If it is the default address, we should not allow unchecking it 
                // because there must always be a default address.
                if (address.isDefault) {
                    binding.switchDefault.isEnabled = false
                }
            }
        } else {
            binding.tvTitle.text = getString(R.string.address_edit_title_add)
            // If it's the first address, make it default automatically and lock it.
            if (hasNoAddress) {
                binding.switchDefault.isChecked = true
                binding.switchDefault.isEnabled = false
            } else {
                binding.switchDefault.isChecked = false
                binding.switchDefault.isEnabled = true
            }
        }

        updateLabelUI()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnHome.setOnClickListener {
            selectedLabel = "Nhà riêng"
            updateLabelUI()
        }

        binding.btnWork.setOnClickListener {
            selectedLabel = "Công ty"
            updateLabelUI()
        }

        binding.btnSave.setOnClickListener {
            saveAddress()
        }
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

        // If setting this address as default, unset others first.
        if (isDefault) {
            AddressBookFragment.addresses.forEach { it.isDefault = false }
        }

        if (addressIndex == -1) {
            // Add new address
            val newAddress = AddressBookFragment.Address(
                label = selectedLabel,
                name = name,
                phone = phone,
                detail = detail,
                isDefault = isDefault
            )
            AddressBookFragment.addresses.add(newAddress)
            Toast.makeText(requireContext(), getString(R.string.address_edit_success_add), Toast.LENGTH_SHORT).show()
        } else {
            // Update existing address
            if (addressIndex in AddressBookFragment.addresses.indices) {
                val address = AddressBookFragment.addresses[addressIndex]
                address.name = name
                address.phone = phone
                address.detail = detail
                address.label = selectedLabel
                address.isDefault = isDefault
                Toast.makeText(requireContext(), getString(R.string.address_edit_success_update), Toast.LENGTH_SHORT).show()
            }
        }

        // Guarantee there is at least one default address if the list is not empty
        if (AddressBookFragment.addresses.isNotEmpty()) {
            val defaultExists = AddressBookFragment.addresses.any { it.isDefault }
            if (!defaultExists) {
                AddressBookFragment.addresses[0].isDefault = true
            }
        }

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
