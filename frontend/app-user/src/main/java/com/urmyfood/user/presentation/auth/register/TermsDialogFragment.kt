package com.urmyfood.user.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.urmyfood.user.R
import com.urmyfood.user.databinding.DialogTermsChatBinding

/**
 * BottomSheet dialog that displays the terms of service
 * in a single scrollable message box.
 */
class TermsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogTermsChatBinding? = null
    private val binding get() = _binding!!

    private var onAgreeClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTermsChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Combine all terms messages into one text block
        val termsContent = listOf(
            getString(R.string.terms_msg_1),
            getString(R.string.terms_msg_2),
            getString(R.string.terms_msg_3),
            getString(R.string.terms_msg_4),
            getString(R.string.terms_msg_5),
            getString(R.string.terms_msg_6)
        ).joinToString("\n\n")

        binding.tvTermsContent.text = termsContent

        binding.btnAgree.setOnClickListener {
            onAgreeClickListener?.invoke()
            dismiss()
        }
    }

    fun setOnAgreeClickListener(listener: () -> Unit) {
        onAgreeClickListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TermsDialogFragment"
    }
}
