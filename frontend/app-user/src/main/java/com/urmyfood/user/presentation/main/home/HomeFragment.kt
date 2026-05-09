package com.urmyfood.user.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainHomeBinding
import com.urmyfood.user.util.BrandingHelper

/**
 * Home screen fragment.
 * Displays featured food items, categories, and promotional content.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideHomeViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BrandingHelper.styleAppName(binding.tvLogo)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Notification button → feature in development
        binding.btnNotification.setOnClickListener {
            showFeatureInDevelopment()
        }

        // "Xem tất cả" link → feature in development
        binding.tvSeeAll.setOnClickListener {
            showFeatureInDevelopment()
        }

        // Category icons → feature in development
        binding.catVietnamese.setOnClickListener { showFeatureInDevelopment() }
        binding.catFastFood.setOnClickListener { showFeatureInDevelopment() }
        binding.catCoffee.setOnClickListener { showFeatureInDevelopment() }
        binding.catRestaurant.setOnClickListener { showFeatureInDevelopment() }

        // Featured food cards → feature in development
        binding.cardFeatured1.setOnClickListener { showFeatureInDevelopment() }
        binding.cardFeatured2.setOnClickListener { showFeatureInDevelopment() }

        // Favorite buttons → feature in development
        binding.btnFav1.setOnClickListener { showFeatureInDevelopment() }
        binding.btnFav2.setOnClickListener { showFeatureInDevelopment() }

        // Add to cart buttons → feature in development
        binding.btnAdd1.setOnClickListener { showFeatureInDevelopment() }
        binding.btnAdd2.setOnClickListener { showFeatureInDevelopment() }
    }

    private fun showFeatureInDevelopment() {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
