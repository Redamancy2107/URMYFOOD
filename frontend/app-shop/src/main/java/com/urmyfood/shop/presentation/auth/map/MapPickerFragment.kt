package com.urmyfood.shop.presentation.auth.map

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentMapPickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapPickerFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null

    private val defaultLocation = LatLng(10.7769, 106.7009)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnConfirm.setOnClickListener { confirmLocation() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        map.setOnCameraIdleListener { updateAddressPreview() }
    }

    private fun updateAddressPreview() {
        val target = googleMap?.cameraPosition?.target ?: return
        val ctx = requireContext()
        viewLifecycleOwner.lifecycleScope.launch {
            val address = reverseGeocode(ctx, target.latitude, target.longitude)
            _binding?.tvAddressPreview?.text = address.ifEmpty { "${target.latitude}, ${target.longitude}" }
        }
    }

    private suspend fun reverseGeocode(ctx: Context, lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val results = Geocoder(ctx, Locale("vi", "VN")).getFromLocation(lat, lng, 1)
                results?.firstOrNull()?.getAddressLine(0) ?: ""
            } catch (e: Exception) {
                ""
            }
        }

    private fun confirmLocation() {
        val target = googleMap?.cameraPosition?.target ?: return
        val address = binding.tvAddressPreview.text.toString()
        val result = Bundle().apply {
            putString("address", address)
            putDouble("lat", target.latitude)
            putDouble("lng", target.longitude)
        }
        setFragmentResult("map_result", result)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
