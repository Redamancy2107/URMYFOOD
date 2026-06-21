package com.urmyfood.shared.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min

object ChatEmojiPicker {
    private val emojis = listOf(
        "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😋",
        "😎", "🥰", "😇", "😉", "🙂", "😅", "😆", "😜",
        "🤗", "🤔", "😢", "😭", "😡", "😤", "😴", "🤩",
        "👍", "👎", "👏", "🙏", "💪", "👌", "🤝", "🙌",
        "❤️", "🔥", "✨", "🎉", "⭐", "✅", "☕", "🍜",
        "🍚", "🍔", "🍟", "🍕", "🥤", "🍰", "🧋", "🍽️"
    )

    private var popupWindow: PopupWindow? = null

    fun open(anchor: View, target: EditText) {
        if (Build.VERSION.SDK_INT >= 35 && openSystemEmojiPicker(target)) {
            return
        }
        toggleFallbackPanel(anchor, target)
    }

    fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
    }

    private fun openSystemEmojiPicker(target: EditText): Boolean {
        target.requestFocus()
        val imm = target.context.getSystemService(InputMethodManager::class.java)
        imm?.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)

        val downHandled = target.dispatchKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_EMOJI_PICKER)
        )
        val upHandled = target.dispatchKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_EMOJI_PICKER)
        )
        return downHandled || upHandled
    }

    private fun toggleFallbackPanel(anchor: View, target: EditText) {
        val currentPopup = popupWindow
        if (currentPopup?.isShowing == true) {
            currentPopup.dismiss()
            popupWindow = null
            return
        }

        val context = anchor.context
        val panelHeight = dp(anchor, 220)
        val panelWidth = max(dp(anchor, 280), context.resources.displayMetrics.widthPixels - dp(anchor, 24))
        val grid = GridLayout(context).apply {
            columnCount = 8
            setPadding(dp(anchor, 8), dp(anchor, 8), dp(anchor, 8), dp(anchor, 8))
        }

        emojis.forEach { emoji ->
            val item = TextView(context).apply {
                text = emoji
                textSize = 26f
                gravity = Gravity.CENTER
                includeFontPadding = false
                minWidth = dp(anchor, 44)
                minHeight = dp(anchor, 44)
                setOnClickListener {
                    insertEmoji(target, emoji)
                    target.requestFocus()
                }
            }
            grid.addView(item, GridLayout.LayoutParams().apply {
                width = dp(anchor, 44)
                height = dp(anchor, 44)
                setMargins(dp(anchor, 2), dp(anchor, 2), dp(anchor, 2), dp(anchor, 2))
            })
        }

        val scrollView = ScrollView(context).apply {
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(anchor, 12).toFloat()
                setStroke(dp(anchor, 1), 0xFFE0E0E0.toInt())
            }
            addView(grid, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }

        val imm = context.getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(target.windowToken, 0)

        popupWindow = PopupWindow(scrollView, panelWidth, panelHeight, true).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = dp(anchor, 8).toFloat()
            showAtLocation(anchor.rootView, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(anchor, 72))
        }
    }

    private fun insertEmoji(target: EditText, emoji: String) {
        val editable = target.text ?: return
        val selectionStart = target.selectionStart.takeIf { it >= 0 } ?: editable.length
        val selectionEnd = target.selectionEnd.takeIf { it >= 0 } ?: editable.length
        val start = min(selectionStart, selectionEnd)
        val end = max(selectionStart, selectionEnd)
        editable.replace(start, end, emoji)
        target.setSelection(start + emoji.length)
    }

    private fun dp(view: View, value: Int): Int =
        (value * view.resources.displayMetrics.density).toInt()
}
