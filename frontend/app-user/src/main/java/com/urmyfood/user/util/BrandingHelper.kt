package com.urmyfood.user.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.widget.TextView

/**
 * Utility to style "URMYFOOD" text with dual font weights:
 * "URMY" -> ExtraBold (weight 800), "FOOD" -> Light (weight 300)
 */
object BrandingHelper {

    fun styleAppName(textView: TextView) {
        val appName = "URMYFOOD"
        val spannable = SpannableString(appName)

        // "URMY" (0..3) → ExtraBold (weight 800)
        val extraBoldTypeface = Typeface.create(
            Typeface.create("sans-serif", Typeface.NORMAL),
            800,
            false
        )
        spannable.setSpan(
            TypefaceSpan(extraBoldTypeface),
            0, 4,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Also set BOLD style span as fallback
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0, 4,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // "FOOD" (4..7) → Light (weight 300)
        val lightTypeface = Typeface.create(
            Typeface.create("sans-serif", Typeface.NORMAL),
            300,
            false
        )
        spannable.setSpan(
            TypefaceSpan(lightTypeface),
            4, 8,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }
}
