package com.urmyfood.shop.presentation.auth.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.databinding.FragmentMapPickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.util.Locale

class MapPickerFragment : Fragment() {

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private val defaultLocation = GeoPoint(10.7769, 106.7009)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            moveToCurrentLocation()
        } else {
            moveTo(defaultLocation)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupClickListeners()
        requestLocationOrFallback()
    }

    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
            controller.setZoom(15.0)
            controller.setCenter(defaultLocation)
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    updateAddressPreview()
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    updateAddressPreview()
                    return false
                }
            })
        }
        updateAddressPreview()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnConfirm.setOnClickListener { confirmLocation() }
    }

    private fun requestLocationOrFallback() {
        if (hasLocationPermission()) {
            moveToCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val context = requireContext()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        if (!hasLocationPermission()) {
            moveTo(defaultLocation)
            return
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastLocation != null) {
            moveTo(lastLocation.toGeoPoint())
        } else {
            moveTo(defaultLocation)
        }
    }

    private fun moveTo(point: GeoPoint) {
        binding.mapView.controller.setCenter(point)
        updateAddressPreview()
    }

    private fun updateAddressPreview() {
        val target = binding.mapView.mapCenter
        val lat = target.latitude
        val lng = target.longitude
        val ctx = requireContext()
        viewLifecycleOwner.lifecycleScope.launch {
            val address = reverseGeocode(ctx, lat, lng)
            _binding?.tvAddressPreview?.text = address.ifEmpty { formatCoordinates(lat, lng) }
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
        val target = binding.mapView.mapCenter
        val lat = target.latitude
        val lng = target.longitude
        val address = binding.tvAddressPreview.text.toString().ifBlank { formatCoordinates(lat, lng) }
        val result = Bundle().apply {
            putString("address", address)
            putDouble("lat", lat)
            putDouble("lng", lng)
        }
        setFragmentResult("map_result", result)
        findNavController().navigateUp()
    }

    private fun formatCoordinates(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.6f, %.6f", lat, lng)

    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding.mapView.onDetach()
        super.onDestroyView()
        _binding = null
    }
}
