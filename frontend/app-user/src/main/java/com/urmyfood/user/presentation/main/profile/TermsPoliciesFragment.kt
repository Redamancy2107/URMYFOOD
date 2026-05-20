package com.urmyfood.user.presentation.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.urmyfood.user.R
import com.urmyfood.user.databinding.FragmentTermsPoliciesBinding
import com.urmyfood.user.presentation.auth.register.TermsDialogFragment

class TermsPoliciesFragment : Fragment() {

    private var _binding: FragmentTermsPoliciesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TermsPoliciesViewModel by viewModels {
        TermsPoliciesViewModel.Factory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsPoliciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.menuTermsUsage.setOnClickListener {
            showTermsDialog()
        }

        binding.menuPrivacyPolicy.setOnClickListener {
            showTermsDialog()
        }

        binding.menuRefundPolicy.setOnClickListener {
            showTermsDialog()
        }

        binding.menuRegulations.setOnClickListener {
            showTermsDialog()
        }

        binding.btnContactSupport.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showTermsDialog() {
        val termsDialog = TermsDialogFragment()
        termsDialog.show(parentFragmentManager, TermsDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
