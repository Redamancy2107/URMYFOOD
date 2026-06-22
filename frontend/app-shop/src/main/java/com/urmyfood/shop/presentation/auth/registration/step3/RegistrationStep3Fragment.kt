package com.urmyfood.shop.presentation.auth.registration.step3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.urmyfood.shop.presentation.common.safeNavigate
import androidx.recyclerview.widget.GridLayoutManager
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentRegistrationStep3Binding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel.Field
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel.Step3UiState

class RegistrationStep3Fragment : Fragment() {

    private var _binding: FragmentRegistrationStep3Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ShopRegistrationFlowViewModel by navGraphViewModels(R.id.nav_registration_flow) {
        ServiceLocator.provideShopRegistrationFlowViewModelFactory()
    }

    private lateinit var photoAdapter: ShopPhotoAdapter

    private val pickPhotos = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addShopPhotos(uris.map { it.toString() })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistrationStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        photoAdapter = ShopPhotoAdapter(
            maxPhotos = ShopRegistrationFlowViewModel.MAX_PHOTOS,
            onRemoveClick = { index -> viewModel.removeShopPhoto(index) },
            onAddClick = { pickPhotos.launch("image/*") }
        )
        binding.rvShopPhotos.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            binding.btnSubmit.isEnabled = false
            viewModel.validateStep3()
        }
    }

    private fun observeViewModel() {
        viewModel.shopPhotoUris.observe(viewLifecycleOwner) { uris ->
            photoAdapter.submitList(uris.toList())
        }

        viewModel.step3UiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Step3UiState.Idle -> {
                    binding.btnSubmit.isEnabled = true
                    binding.tvPhotosError.visibility = View.GONE
                }
                is Step3UiState.ValidationError -> {
                    binding.btnSubmit.isEnabled = true
                    state.fields[Field.SHOP_PHOTOS]?.let { msg ->
                        binding.tvPhotosError.text = msg
                        binding.tvPhotosError.visibility = View.VISIBLE
                    }
                }
                is Step3UiState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                }
                is Step3UiState.Success -> {
                    viewModel.resetStep3State()
                    findNavController().safeNavigate(R.id.action_step3_to_verification_pending)
                }
                is Step3UiState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.tvPhotosError.text = state.message
                    binding.tvPhotosError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
