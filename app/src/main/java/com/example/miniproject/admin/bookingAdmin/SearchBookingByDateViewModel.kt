package com.example.miniproject.admin.bookingAdmin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchBookingByDateViewModel : ViewModel() {

    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    val selectedDateMillis = _selectedDateMillis.asStateFlow()

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    fun setSelectedDate(millis: Long?) {
        _selectedDateMillis.value = millis
    }

    fun getFormattedDate(): String {
        return _selectedDateMillis.value?.let {
            dateFormat.format(Date(it))
        } ?: ""
    }
}
