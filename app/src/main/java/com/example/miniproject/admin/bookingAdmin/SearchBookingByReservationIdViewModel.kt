package com.example.miniproject.admin.bookingAdmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.SearchHistoryRepository
import com.example.miniproject.reservation.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class for search result with full details
data class ReservationDetail(
    val reservation: Reservation,
    val facilityName: String,
    val userDisplayID: String
)

class SearchBookingByReservationIdViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val historyKey = "booking_by_id_search_history"
    private val db = FirebaseFirestore.getInstance()

    // Search text and history
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    // Search result (single reservation)
    private val _searchResult = MutableStateFlow<ReservationDetail?>(null)
    val searchResult = _searchResult.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        // Clear previous results when user starts typing new search
        if (text.isEmpty()) {
            _searchResult.value = null
            _error.value = null
        }
    }

    // Search for reservation by ID
    fun searchReservation(reservationId: String) {
        if (reservationId.isBlank()) {
            _error.value = "Please enter a reservation ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Search for the reservation
                val reservationDoc = db.collection("reservation")
                    .document(reservationId.trim())
                    .get()
                    .await()

                if (!reservationDoc.exists()) {
                    _error.value = "Reservation not found"
                    _searchResult.value = null
                    return@launch
                }

                // Get facility ID and strip it
                val fullFacilityId = reservationDoc.getString("facilityID")
                    ?: reservationDoc.getString("facility_id")
                    ?: reservationDoc.getString("facility")
                    ?: ""

                val strippedFacilityId = fullFacilityId.substringBefore("_")

                // Create Reservation object
                val reservation = Reservation(
                    id = reservationDoc.id,
                    facilityID = fullFacilityId,
                    userID = reservationDoc.getString("userID") ?: "",
                    bookedTime = reservationDoc.getTimestamp("bookedTime"),
                    bookedHours = reservationDoc.getDouble("bookedHours") ?: 1.0
                )

                // Get facility name
                val facilityName = try {
                    val facilityDoc = db.collection("facility")
                        .document(strippedFacilityId)
                        .get()
                        .await()
                    facilityDoc.getString("name") ?: "Unknown Facility"
                } catch (e: Exception) {
                    println("⚠️ Could not fetch facility $strippedFacilityId: ${e.message}")
                    "Unknown Facility"
                }

                // Get user display ID
                val userId = reservationDoc.getString("userID") ?: ""

                val userDisplayID = if (userId.isNotBlank()) {
                    try {
                        println("🔍 Looking for user with displayId: $userId")

                        // Query by displayId field
                        val userQuery = db.collection("user")
                            .whereEqualTo("displayId", userId)
                            .limit(1)
                            .get()
                            .await()

                        if (userQuery.isEmpty) {
                            println("❌ User not found for displayId: $userId")
                            userId  // Just show the displayId
                        } else {
                            val userDoc = userQuery.documents[0]
                            val name = userDoc.getString("name") ?: ""
                            val displayId = userDoc.getString("displayId") ?: userId

                            println("✅ Found user: $name (ID: $displayId)")

                            // ✅ Show both: "Wangsa (ID: 3)" or just "3" if no name
                            if (name.isNotBlank()) {
                                "$name (ID: $displayId)"
                            } else {
                                displayId
                            }
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error fetching user $userId: ${e.message}")
                        userId
                    }
                } else {
                    "No User ID"
                }

                _searchResult.value = ReservationDetail(
                    reservation = reservation,
                    facilityName = facilityName,
                    userDisplayID = userDisplayID
                )

                // Add to search history
                addSearchToHistory(reservationId.trim())

                println("✅ Found reservation: $reservationId")
            } catch (e: Exception) {
                _error.value = "Failed to search reservation: ${e.message}"
                _searchResult.value = null
                println("❌ Error searching reservation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete reservation
    fun deleteReservation(reservationId: String) {
        viewModelScope.launch {
            try {
                db.collection("reservation")
                    .document(reservationId)
                    .delete()
                    .await()

                // Clear search result
                _searchResult.value = null
                _searchText.value = ""

                println("✅ Reservation deleted: $reservationId")
            } catch (e: Exception) {
                _error.value = "Failed to delete reservation: ${e.message}"
                println("❌ Error deleting reservation: ${e.message}")
            }
        }
    }

    // Search history management
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

    // Error handling
    fun clearError() {
        _error.value = null
    }
}