package com.example.miniproject.data

import android.content.Context
import android.content.SharedPreferences

class SearchHistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val separator = "___HISTORY_ITEM___"

    fun getHistory(historyKey: String): List<String> {
        val savedHistory = prefs.getString(historyKey, "") ?: ""
        return if (savedHistory.isEmpty()) {
            emptyList()
        } else {
            savedHistory.split(separator)
        }
    }

    fun addToHistory(historyKey: String, searchTerm: String) {
        if (searchTerm.isBlank()) return

        val currentHistory = getHistory(historyKey).toMutableList()
        currentHistory.remove(searchTerm)
        currentHistory.add(0, searchTerm)
        val updatedHistory = currentHistory.take(10)

        prefs.edit().putString(historyKey, updatedHistory.joinToString(separator)).apply()
    }

    fun clearHistoryItem(historyKey: String, item: String) {
        val currentHistory = getHistory(historyKey).toMutableList()
        currentHistory.remove(item)
        prefs.edit().putString(historyKey, currentHistory.joinToString(separator)).apply()
    }

    fun clearAllHistory(historyKey: String) {
        prefs.edit().remove(historyKey).apply()
    }
}
