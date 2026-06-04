package com.urmyfood.shop.presentation.auth.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentVerificationPendingBinding
import com.urmyfood.shop.presentation.common.safeNavigate

class VerificationPendingFragment : Fragment() {

    private var _binding: FragmentVerificationPendingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBackToHome.setOnClickListener {
            findNavController().safeNavigate(R.id.action_verification_pending_to_main)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
