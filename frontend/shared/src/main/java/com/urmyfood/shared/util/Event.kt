package com.urmyfood.shared.util

/**
 * Wraps a value that should be consumed exactly once by an observer.
 * Prevents LiveData sticky-value re-delivery when a Fragment re-observes after onPause/onResume.
 */
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again, or null if already consumed.
     */
    fun getContentIfNotHandled(): T? =
        if (hasBeenHandled) null
        else { hasBeenHandled = true; content }

    /** Peek at content without consuming it. */
    fun peekContent(): T = content
}
