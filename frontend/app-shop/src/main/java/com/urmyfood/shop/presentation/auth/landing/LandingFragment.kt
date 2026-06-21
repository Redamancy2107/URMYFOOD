package com.urmyfood.shop.presentation.auth.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.FragmentAuthLandingBinding
import com.urmyfood.shop.di.ServiceLocator
import com.urmyfood.shop.presentation.common.safeNavigate

class LandingFragment : Fragment() {

    private var _binding: FragmentAuthLandingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            ServiceLocator.tokenManager.setFirstTime(false)
            findNavController().safeNavigate(R.id.action_landing_to_login)
        }
        binding.btnRegister.setOnClickListener {
            ServiceLocator.tokenManager.setFirstTime(false)
            findNavController().safeNavigate(R.id.action_landing_to_register)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
