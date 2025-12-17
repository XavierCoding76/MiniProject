package com.example.miniproject.booking.Navigation

import androidx.lifecycle.ViewModel
import com.example.miniproject.booking.UI.BookingHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookingHistoryViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<BookingHistoryItem>>(emptyList())
    val items = _items.asStateFlow()

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        _items.value = listOf(
            BookingHistoryItem(
                facilityName = "TA205, Arena TAR UMT",
                date = "11/11/2025",
                time = "11:00AM - 12:00PM"
            ),
            BookingHistoryItem(
                facilityName = "Discussion Room A001, Library",
                date = "12/11/2025",
                time = "11:00AM - 12:00PM"
            )
        )
    }
}
