package com.urmyfood.shop.presentation.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.shop.databinding.FragmentWrongRoleBottomSheetBinding

class WrongRoleBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentWrongRoleBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWrongRoleBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener {
            parentFragmentManager.setFragmentResult(KEY, Bundle.EMPTY)
            dismiss()
        }
        binding.tvBackToLogin.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WrongRoleBottomSheet"
        const val KEY = "wrong_role_register"
        fun newInstance() = WrongRoleBottomSheetFragment()
    }
}
