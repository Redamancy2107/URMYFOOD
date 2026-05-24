package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.domain.repository.SearchHistoryRepository

class SearchHistoryManager : SearchHistoryRepository {
    private val prefs: SharedPreferences
    private val gson = Gson()

    constructor(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    internal constructor(prefs: SharedPreferences) {
        this.prefs = prefs
    }

    override fun getHistory(): List<String> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    override fun addQuery(query: String): List<String> {
        val normalized = query.trim().replace(Regex("\\s+"), " ")
        if (normalized.isBlank()) {
            return getHistory()
        }
        val updated = mutableListOf(normalized)
        updated.addAll(getHistory().filterNot { it.equals(normalized, ignoreCase = true) })
        return saveAndReturn(updated.take(MAX_HISTORY))
    }

    override fun removeQuery(query: String): List<String> {
        return saveAndReturn(getHistory().filterNot { it.equals(query, ignoreCase = true) })
    }

    override fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveAndReturn(history: List<String>): List<String> {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply()
        return history
    }

    companion object {
        private const val PREF_NAME = "urmyfood_search_history_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_HISTORY = 10
    }
}
