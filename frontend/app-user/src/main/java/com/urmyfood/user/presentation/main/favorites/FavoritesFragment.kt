package com.urmyfood.user.presentation.main.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainFavoritesBinding

/**
 * Favorites screen fragment.
 * Displays the list of user's favorite food items.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentMainFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideFavoritesViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Food card clicks → feature in development
        binding.cardFav1.setOnClickListener { showFeatureInDevelopment() }
        binding.cardFav2.setOnClickListener { showFeatureInDevelopment() }
        binding.cardFav3.setOnClickListener { showFeatureInDevelopment() }
        binding.cardFav4.setOnClickListener { showFeatureInDevelopment() }

        // Add to cart buttons → feature in development
        binding.btnAddFav1.setOnClickListener { showFeatureInDevelopment() }
        binding.btnAddFav2.setOnClickListener { showFeatureInDevelopment() }
        binding.btnAddFav3.setOnClickListener { showFeatureInDevelopment() }
        binding.btnAddFav4.setOnClickListener { showFeatureInDevelopment() }
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
