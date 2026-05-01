package com.urmyfood.user.util

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Custom TextView that automatically applies URMYFOOD branding style.
 * "URMY" → ExtraBold (weight 800), "FOOD" → Light (weight 300)
 */
class BrandingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        applyBranding()
    }

    private fun applyBranding() {
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

        text = spannable
    }
}
