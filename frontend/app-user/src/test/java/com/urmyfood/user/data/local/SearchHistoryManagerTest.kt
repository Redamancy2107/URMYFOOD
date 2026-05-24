package com.urmyfood.user.data.local

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHistoryManagerTest {

    @Test
    fun `addQuery stores newest first`() {
        val manager = SearchHistoryManager(FakeSharedPreferences())

        manager.addQuery("bun")
        manager.addQuery("pho")

        assertEquals(listOf("pho", "bun"), manager.getHistory())
    }

    @Test
    fun `addQuery trims spaces and deduplicates ignoring case`() {
        val manager = SearchHistoryManager(FakeSharedPreferences())

        manager.addQuery("  bun   bo  ")
        manager.addQuery("BUN BO")

        assertEquals(listOf("BUN BO"), manager.getHistory())
    }

    @Test
    fun `addQuery ignores blank query`() {
        val manager = SearchHistoryManager(FakeSharedPreferences())

        manager.addQuery("   ")

        assertTrue(manager.getHistory().isEmpty())
    }

    @Test
    fun `addQuery keeps maximum ten items`() {
        val manager = SearchHistoryManager(FakeSharedPreferences())

        repeat(12) { index -> manager.addQuery("query $index") }

        val history = manager.getHistory()
        assertEquals(10, history.size)
        assertEquals("query 11", history.first())
        assertEquals("query 2", history.last())
    }

    @Test
    fun `removeQuery and clearHistory update storage`() {
        val manager = SearchHistoryManager(FakeSharedPreferences())

        manager.addQuery("bun")
        manager.addQuery("pho")
        manager.removeQuery("BUN")

        assertEquals(listOf("pho"), manager.getHistory())

        manager.clearHistory()
        assertTrue(manager.getHistory().isEmpty())
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()
        private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        override fun getAll(): MutableMap<String, *> = values

        override fun getString(key: String?, defValue: String?): String? = values[key] as? String ?: defValue

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues

        override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue

        override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue

        override fun contains(key: String?): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = FakeEditor(values, listeners)

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            listener?.let { listeners.add(it) }
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            listener?.let { listeners.remove(it) }
        }
    }

    private class FakeEditor(
        private val values: MutableMap<String, Any?>,
        private val listeners: Set<SharedPreferences.OnSharedPreferenceChangeListener>
    ) : SharedPreferences.Editor {
        private val updates = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        private var clear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) updates[key] = value
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this

        override fun remove(key: String?): SharedPreferences.Editor {
            key?.let { removals.add(it) }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clear = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            val changedKeys = mutableSetOf<String>()
            if (clear) {
                changedKeys.addAll(values.keys)
                values.clear()
            }
            removals.forEach { key ->
                if (values.containsKey(key)) changedKeys.add(key)
                values.remove(key)
            }
            updates.forEach { (key, value) ->
                values[key] = value
                changedKeys.add(key)
            }
            changedKeys.forEach { key ->
                listeners.forEach { it.onSharedPreferenceChanged(null, key) }
            }
        }
    }
}
