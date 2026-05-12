package com.urmyfood.user.presentation.main.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentMainSearchBinding

/**
 * Search screen fragment.
 * Displays search bar, recent searches, suggestion chips, and popular categories.
 */
class SearchFragment : Fragment() {

    private var _binding: FragmentMainSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels {
        com.urmyfood.user.di.ServiceLocator.provideSearchViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener { showFeatureInDevelopment() }

        // "Xóa tất cả" → feature in development
        binding.tvClearAll.setOnClickListener {
            showFeatureInDevelopment()
        }

        // Popular category cards → feature in development
        binding.catSeafood.setOnClickListener { showFeatureInDevelopment() }
        binding.catSnacks.setOnClickListener { showFeatureInDevelopment() }
        binding.catEatclean.setOnClickListener { showFeatureInDevelopment() }
        binding.catDrinks.setOnClickListener { showFeatureInDevelopment() }

        // Search bar action → feature in development
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            showFeatureInDevelopment()
            true
        }
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
