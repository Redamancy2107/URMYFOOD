package com.urmyfood.user.presentation.common

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.urmyfood.user.R

class GuestLoginDialog : DialogFragment() {

    var onLoginClick: (() -> Unit)? = null
    var onRegisterClick: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.guest_dialog_title)
            .setMessage(R.string.guest_dialog_message)
            .setPositiveButton(R.string.guest_dialog_login) { _, _ -> onLoginClick?.invoke() }
            .setNegativeButton(R.string.guest_dialog_register) { _, _ -> onRegisterClick?.invoke() }
            .setNeutralButton(R.string.guest_dialog_cancel, null)
            .create()
    }

    companion object {
        const val TAG = "GuestLoginDialog"
    }
}
