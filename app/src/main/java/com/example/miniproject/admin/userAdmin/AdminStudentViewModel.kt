package com.example.miniproject.admin.userAdmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.components.SearchResultItemData
import com.example.miniproject.data.SearchHistoryRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class AdminStudentViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val authRepository = AuthRepository()
    private val historyKey = "student_search_history"

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItemData>?>(null)
    val searchResults = _searchResults.asStateFlow()

    init {
        _searchHistory.value = historyRepository.getHistory(historyKey)

        viewModelScope.launch {
            searchText
                .debounce(500) // Debounce for 500ms
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        val user = authRepository.findUserByDisplayId(query)
        _searchResults.value =
            user?.let {
                listOf(
                    SearchResultItemData(
                        id = it.id,
                        title = "${it.name} (${it.displayId})"
                    )
                )
            } ?: emptyList()
    }


    fun onClearHistoryItem(item: String) {
        historyRepository.clearHistoryItem(historyKey, item)
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun addSearchToHistory(searchTerm: String) {
        if (searchTerm.isNotBlank()) {
            historyRepository.addToHistory(historyKey, searchTerm)
            _searchHistory.value = historyRepository.getHistory(historyKey)
        }
    }

    fun clearAllHistory() {
        historyRepository.clearAllHistory(historyKey)
        _searchHistory.value = emptyList()
    }
}
