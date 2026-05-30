package com.urmyfood.shop.presentation.auth.registration.step1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentRegistrationStep1Binding
import com.urmyfood.shop.domain.model.ShopCategory
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel.Field
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel.Step1UiState

class RegistrationStep1Fragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRegistrationStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ShopRegistrationFlowViewModel by navGraphViewModels(R.id.nav_registration_flow) {
        ShopRegistrationFlowViewModel.Factory()
    }

    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistrationStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapViewPreview.onCreate(savedInstanceState)
        binding.mapViewPreview.getMapAsync(this)
        setupClickListeners()
        observeViewModel()
        setupFragmentResultListener()
        restoreUiState()
    }

    private fun setupClickListeners() {
        binding.rowCategory.setOnClickListener { showCategoryPicker() }
        binding.btnMapPicker.setOnClickListener {
            syncFieldsToViewModel()
            findNavController().navigate(R.id.action_step1_to_map)
        }
        binding.btnNext.setOnClickListener {
            syncFieldsToViewModel()
            viewModel.validateStep1()
        }
    }

    private fun showCategoryPicker() {
        val categories = ShopCategory.entries.toTypedArray()
        val names = categories.map { it.displayName }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn danh mục món ăn")
            .setItems(names) { _, which ->
                viewModel.selectCategory(categories[which])
                binding.tvCategoryValue.text = categories[which].displayName
                binding.tvCategoryValue.setTextColor(requireContext().getColor(R.color.text_primary))
                binding.tvCategoryError.visibility = View.GONE
            }
            .show()
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("map_result") { _, bundle ->
            val addr = bundle.getString("address", "")
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")
            viewModel.setAddress(addr, lat, lng)
            binding.etAddress.setText(addr)
            binding.tvAddressError.visibility = View.GONE
            showMapPreview(lat, lng)
        }
    }

    private fun showMapPreview(lat: Double, lng: Double) {
        binding.mapViewPreview.visibility = View.VISIBLE
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
    }

    private fun restoreUiState() {
        viewModel.selectedCategory.value?.let { cat ->
            binding.tvCategoryValue.text = cat.displayName
            binding.tvCategoryValue.setTextColor(requireContext().getColor(R.color.text_primary))
        }
        viewModel.address.value?.takeIf { it.isNotBlank() }?.let { addr ->
            binding.etAddress.setText(addr)
        }
        viewModel.shopName.value?.takeIf { it.isNotBlank() }?.let { name ->
            binding.etShopName.setText(name)
        }
        val lat = viewModel.latitude.value
        val lng = viewModel.longitude.value
        if (lat != null && lng != null) {
            showMapPreview(lat, lng)
        }
    }

    private fun syncFieldsToViewModel() {
        viewModel.setShopName(binding.etShopName.text.toString())
        viewModel.updateAddressText(binding.etAddress.text.toString())
    }

    private fun observeViewModel() {
        viewModel.step1UiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Step1UiState.Idle -> clearErrors()
                is Step1UiState.ValidationError -> {
                    clearErrors()
                    state.fields.forEach { (field, msg) ->
                        when (field) {
                            Field.SHOP_NAME -> showError(binding.tvShopNameError, msg)
                            Field.CATEGORY -> showError(binding.tvCategoryError, msg)
                            Field.ADDRESS -> showError(binding.tvAddressError, msg)
                            else -> Unit
                        }
                    }
                }
                is Step1UiState.Proceed -> {
                    viewModel.resetStep1State()
                    findNavController().navigate(R.id.action_step1_to_step2)
                }
            }
        }
    }

    private fun showError(textView: android.widget.TextView, message: String) {
        textView.text = message
        textView.visibility = View.VISIBLE
    }

    private fun clearErrors() {
        binding.tvShopNameError.visibility = View.GONE
        binding.tvCategoryError.visibility = View.GONE
        binding.tvAddressError.visibility = View.GONE
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.setAllGesturesEnabled(false)
        val lat = viewModel.latitude.value
        val lng = viewModel.longitude.value
        if (lat != null && lng != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
        }
    }

    // MapView lifecycle delegation
    override fun onStart() {
        super.onStart()
        binding.mapViewPreview.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewPreview.onResume()
    }

    override fun onPause() {
        binding.mapViewPreview.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapViewPreview.onStop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.mapViewPreview?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.mapViewPreview.onDestroy()
        super.onDestroyView()
        _binding = null
    }
}
