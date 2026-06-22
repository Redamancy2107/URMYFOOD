package com.urmyfood.shared.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.urmyfood.shared.R

object CustomToast {

    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        val activity = context as? Activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            val rootView = activity.findViewById<android.view.View>(android.R.id.content)
            if (rootView != null) {
                val snackbarDuration = if (duration == Toast.LENGTH_LONG) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
                val snackbar = Snackbar.make(rootView, message, snackbarDuration)
                
                // Style Snackbar to white background, black text
                snackbar.setBackgroundTint(0xFFFFFFFF.toInt())
                snackbar.setTextColor(0xFF000000.toInt())
                snackbar.show()
                return
            }
        }

        // Fallback to Toast
        try {
            val toast = Toast(context.applicationContext)
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(R.layout.layout_custom_toast, null)
            val text = layout.findViewById<TextView>(R.id.toast_message)
            text.text = message
            toast.view = layout
            toast.duration = duration
            toast.show()
        } catch (e: Exception) {
            // Fallback to standard Toast
            Toast.makeText(context.applicationContext, message, duration).show()
        }
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    CustomToast.show(this, message, duration)
}

fun androidx.fragment.app.Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.let { CustomToast.show(it, message, duration) }
}
