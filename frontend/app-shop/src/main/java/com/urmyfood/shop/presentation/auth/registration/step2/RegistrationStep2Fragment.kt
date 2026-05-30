package com.urmyfood.shop.presentation.auth.registration.step2

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentRegistrationStep2Binding
import com.urmyfood.shop.databinding.ItemImageUploadCardBinding
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel.Field
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel.Step2UiState

class RegistrationStep2Fragment : Fragment() {

    private var _binding: FragmentRegistrationStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ShopRegistrationFlowViewModel by navGraphViewModels(R.id.nav_registration_flow) {
        ShopRegistrationFlowViewModel.Factory()
    }

    private val pickCccdFront = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadImage(it, binding.cardCccdFront, Field.CCCD_FRONT) }
    }
    private val pickCccdBack = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadImage(it, binding.cardCccdBack, Field.CCCD_BACK) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistrationStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        restoreUiState()
    }

    private fun setupClickListeners() {
        binding.cardCccdFront.root.setOnClickListener { pickCccdFront.launch("image/*") }
        binding.cardCccdBack.root.setOnClickListener { pickCccdBack.launch("image/*") }
        binding.btnNext.setOnClickListener { viewModel.validateStep2() }
    }

    private fun restoreUiState() {
        viewModel.cccdFrontUri.value?.let { uri ->
            showPreview(binding.cardCccdFront, Uri.parse(uri))
        }
        viewModel.cccdBackUri.value?.let { uri ->
            showPreview(binding.cardCccdBack, Uri.parse(uri))
        }
    }

    private fun loadImage(uri: Uri, cardBinding: ItemImageUploadCardBinding, field: Field) {
        when (field) {
            Field.CCCD_FRONT -> {
                viewModel.setCccdFront(uri.toString())
                binding.tvCccdFrontError.visibility = View.GONE
            }
            Field.CCCD_BACK -> {
                viewModel.setCccdBack(uri.toString())
                binding.tvCccdBackError.visibility = View.GONE
            }
            else -> Unit
        }
        showPreview(cardBinding, uri)
    }

    private fun showPreview(cardBinding: ItemImageUploadCardBinding, uri: Uri) {
        cardBinding.ivPlaceholder.visibility = View.GONE
        cardBinding.ivPreview.visibility = View.VISIBLE
        Glide.with(this).load(uri).centerCrop().into(cardBinding.ivPreview)
    }

    private fun observeViewModel() {
        viewModel.step2UiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Step2UiState.Idle -> clearErrors()
                is Step2UiState.ValidationError -> {
                    clearErrors()
                    state.fields.forEach { (field, msg) ->
                        when (field) {
                            Field.CCCD_FRONT -> showError(binding.tvCccdFrontError, msg)
                            Field.CCCD_BACK -> showError(binding.tvCccdBackError, msg)
                            else -> Unit
                        }
                    }
                }
                is Step2UiState.Proceed -> {
                    viewModel.resetStep2State()
                    findNavController().navigate(R.id.action_step2_to_step3)
                }
            }
        }
    }

    private fun showError(textView: android.widget.TextView, message: String) {
        textView.text = message
        textView.visibility = View.VISIBLE
    }

    private fun clearErrors() {
        binding.tvCccdFrontError.visibility = View.GONE
        binding.tvCccdBackError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
