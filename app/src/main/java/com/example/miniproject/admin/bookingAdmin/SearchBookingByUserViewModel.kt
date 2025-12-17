package com.example.miniproject.admin.bookingAdmin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.SearchHistoryRepository
import com.example.miniproject.reservation.Reservation
import com.example.miniproject.reservation.ReservationRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchBookingByUserViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository by lazy { SearchHistoryRepository(application) }
    private val reservationRepository by lazy { ReservationRepository() }

    private val historyKey = "booking_by_user_search_history"

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Reservation>?>(null)
    val searchResults = _searchResults.asStateFlow()

    init {
        viewModelScope.launch {
            _searchHistory.value = historyRepository.getHistory(historyKey)

            searchText
                .debounce(500)
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
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

    private fun performSearch(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = null // Set to null to hide the list
                return@launch
            }
            Log.d("SearchVM", "Performing search for userID: '$query'")
            val results = reservationRepository.findReservationsByUserId(query)
            Log.d("SearchVM", "Found ${results.size} reservations for query: '$query'")
            _searchResults.value = results
        }
    }
}
