package com.urmyfood.shop.presentation.common

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.urmyfood.shop.R
import com.urmyfood.shop.databinding.DialogTermsChatBinding

/**
 * DialogFragment that displays the terms of service
 * in a center pop-up for app-shop.
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

        val title = arguments?.getString(ARG_TITLE) ?: getString(R.string.terms_dialog_title)
        binding.tvTermsTitle.text = title

        // Combine all terms messages into one text block if content is not passed
        val termsContent = arguments?.getString(ARG_CONTENT) ?: listOf(
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
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"

        fun newInstance(title: String, content: String): TermsDialogFragment {
            val fragment = TermsDialogFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_CONTENT, content)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
