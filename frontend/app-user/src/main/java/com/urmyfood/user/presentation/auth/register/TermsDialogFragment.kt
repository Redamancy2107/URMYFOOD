package com.urmyfood.user.presentation.auth.register

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.urmyfood.user.R
import com.urmyfood.user.databinding.DialogTermsChatBinding

/**
 * DialogFragment that displays the terms of service
 * in a center pop-up.
 */
class TermsDialogFragment : DialogFragment() {

    private var _binding: DialogTermsChatBinding? = null
    private val binding get() = _binding!!

    private var onAgreeClickListener: (() -> Unit)? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

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
            getString(R.string.terms_msg_6),
            getString(R.string.terms_msg_7),
            getString(R.string.terms_msg_8),
            getString(R.string.terms_msg_9),
            getString(R.string.terms_msg_10)
        ).joinToString("<br><br>")

        binding.tvTermsContent.text = Html.fromHtml(termsContent, Html.FROM_HTML_MODE_LEGACY)

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
