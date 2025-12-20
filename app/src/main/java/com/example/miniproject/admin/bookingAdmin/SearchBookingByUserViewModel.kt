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

// Data class for search results with user details
data class ReservationWithUserDetails(
    val reservation: Reservation,
    val facilityName: String,
    val userName: String,
    val userDisplayId: String
)

class SearchBookingByUserViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val historyKey = "booking_by_user_search_history"
    private val db = FirebaseFirestore.getInstance()

    // Search text and history
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<List<ReservationWithUserDetails>?>(null)
    val searchResults = _searchResults.asStateFlow()

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
        if (text.isEmpty()) {
            _searchResults.value = null
            _error.value = null
        }
    }

    // Search for reservations by user (displayId or name)
    fun searchReservationsByUser(searchQuery: String) {
        if (searchQuery.isBlank()) {
            _error.value = "Please enter a user ID or name"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val query = searchQuery.trim()
                println("🔍 Searching for user: $query")

                // First, find users matching the search query
                val userQuery = db.collection("user")
                    .get()
                    .await()

                val matchingUserIds = mutableSetOf<String>()
                val userDataMap = mutableMapOf<String, Pair<String, String>>() // userId -> (name, displayId)

                for (userDoc in userQuery.documents) {
                    val displayId = userDoc.getString("displayId") ?: ""
                    val name = userDoc.getString("name") ?: ""
                    val userId = userDoc.id

                    // Check if user matches search query
                    if (displayId.contains(query, ignoreCase = true) ||
                        name.contains(query, ignoreCase = true) ||
                        userId.contains(query, ignoreCase = true)) {
                        matchingUserIds.add(displayId)
                        userDataMap[displayId] = Pair(name, displayId)
                        println("✅ Found matching user: $name (ID: $displayId)")
                    }
                }

                if (matchingUserIds.isEmpty()) {
                    _error.value = "No users found matching '$query'"
                    _searchResults.value = emptyList()
                    return@launch
                }

                // Now find all reservations for these users
                val reservationsSnapshot = db.collection("reservation")
                    .get()
                    .await()

                val results = mutableListOf<ReservationWithUserDetails>()

                for (doc in reservationsSnapshot.documents) {
                    try {
                        val userId = doc.getString("userID") ?: continue

                        // Check if this reservation belongs to a matching user
                        if (userId in matchingUserIds) {
                            val fullFacilityId = doc.getString("facilityID")
                                ?: doc.getString("facility_id")
                                ?: doc.getString("facility")
                                ?: ""

                            val strippedFacilityId = fullFacilityId.substringBefore("_")

                            val reservation = Reservation(
                                id = doc.id,
                                facilityID = fullFacilityId,
                                userID = userId,
                                bookedTime = doc.getTimestamp("bookedTime"),
                                bookedHours = doc.getDouble("bookedHours") ?: 1.0
                            )

                            // Get facility name
                            val facilityName = try {
                                val facilityDoc = db.collection("facility")
                                    .document(strippedFacilityId)
                                    .get()
                                    .await()
                                facilityDoc.getString("name") ?: "Unknown Facility"
                            } catch (e: Exception) {
                                "Unknown Facility"
                            }

                            // Get user data
                            val userData = userDataMap[userId] ?: Pair("Unknown", userId)

                            results.add(
                                ReservationWithUserDetails(
                                    reservation = reservation,
                                    facilityName = facilityName,
                                    userName = userData.first,
                                    userDisplayId = userData.second
                                )
                            )
                        }
                    } catch (e: Exception) {
                        println("❌ Error processing reservation ${doc.id}: ${e.message}")
                    }
                }

                // Sort by booked time (newest first)
                _searchResults.value = results.sortedByDescending {
                    it.reservation.bookedTime?.toDate()?.time ?: 0L
                }

                // Add to search history
                addSearchToHistory(query)

                println("✅ Found ${results.size} bookings for user: $query")
            } catch (e: Exception) {
                _error.value = "Failed to search bookings: ${e.message}"
                println("❌ Error searching bookings: ${e.message}")
                _searchResults.value = emptyList()
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

                // Refresh search results
                if (_searchText.value.isNotBlank()) {
                    searchReservationsByUser(_searchText.value)
                }

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