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

// Data class for Facility
data class FacilityOption(
    val id: String = "",
    val name: String = ""
)

// Data class for search results
data class ReservationWithFacilityInfo(
    val reservation: Reservation,
    val facilityName: String,
    val userDisplayID: String
)

// ViewModel
class SearchBookingByFacilityViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val historyKey = "booking_by_facility_search_history"
    private val db = FirebaseFirestore.getInstance()

    // Search text and history
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    // Facility data
    private val _facilities = MutableStateFlow<List<FacilityOption>>(emptyList())
    val facilities = _facilities.asStateFlow()

    private val _selectedFacility = MutableStateFlow<FacilityOption?>(null)
    val selectedFacility = _selectedFacility.asStateFlow()

    private val _filteredFacilities = MutableStateFlow<List<FacilityOption>>(emptyList())
    val filteredFacilities = _filteredFacilities.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<List<ReservationWithFacilityInfo>?>(null)
    val searchResults = _searchResults.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        _searchHistory.value = historyRepository.getHistory(historyKey)
        loadFacilities()
    }

    // Load facilities from Firebase
    private fun loadFacilities() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("facility").get().await()
                val facilityList = snapshot.documents.mapNotNull { doc ->
                    FacilityOption(
                        id = doc.id,
                        name = doc.getString("name") ?: doc.id
                    )
                }
                _facilities.value = facilityList.sortedBy { it.name }
                _filteredFacilities.value = facilityList
                println("✅ Loaded ${facilityList.size} facilities")
            } catch (e: Exception) {
                _error.value = "Failed to load facilities: ${e.message}"
                println("❌ Error loading facilities: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Search text change handler
    fun onSearchTextChange(text: String) {
        _searchText.value = text
        filterFacilities(text)
    }

    // Filter facilities based on search query
    private fun filterFacilities(query: String) {
        _filteredFacilities.value = if (query.isBlank()) {
            _facilities.value
        } else {
            _facilities.value.filter { facility ->
                facility.name.contains(query, ignoreCase = true) ||
                        facility.id.contains(query, ignoreCase = true)
            }
        }
    }

    // Set selected facility and search bookings
    fun setSelectedFacility(facility: FacilityOption) {
        _selectedFacility.value = facility
        _searchText.value = facility.name
        // Add to search history
        addSearchToHistory(facility.name)
        // Automatically search bookings
        searchBookingsByFacility(facility.id)
    }

    // Clear selection
    fun clearSelection() {
        _selectedFacility.value = null
        _searchText.value = ""
        _filteredFacilities.value = _facilities.value
        _searchResults.value = null
    }

    // Search bookings for selected facility
    private fun searchBookingsByFacility(facilityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get ALL reservations since we need to filter by stripped facility ID
                val reservationsSnapshot = db.collection("reservation")
                    .get()
                    .await()

                val results = mutableListOf<ReservationWithFacilityInfo>()

                for (doc in reservationsSnapshot.documents) {
                    try {
                        // Get facility ID and strip everything after underscore
                        val fullFacilityId = doc.getString("facilityID")
                            ?: doc.getString("facility_id")
                            ?: doc.getString("facility")
                            ?: ""

                        // Strip away underscore and everything after it
                        val strippedFacilityId = fullFacilityId.substringBefore("_")

                        // Only include if it matches the selected facility
                        if (strippedFacilityId == facilityId) {
                            // Manually create Reservation object without using toObject()
                            val reservation = Reservation(
                                id = doc.id,
                                facilityID = fullFacilityId,
                                userID = doc.getString("userID") ?: "",
                                bookedTime = doc.getTimestamp("bookedTime"),
                                bookedHours = doc.getDouble("bookedHours") ?: 1.0
                            )

                            val facilityName = _facilities.value
                                .find { it.id == strippedFacilityId }?.name
                                ?: "Unknown Facility"

//                            // Get user display ID
//                            val userId = doc.getString("userID")
//                                ?: doc.getString("user_id")
//                                ?: doc.getString("user")
//                                ?: ""
//                            val userDoc = db.collection("user")
//                                .document(userId)
//                                .get()
//                                .await()
//                            val userDisplayID = userDoc.getString("displayId") ?: "Unknown User"
                            val userId = doc.getString("userID") ?: ""

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
                                println("⚠️ No userID in reservation ${doc.id}")
                                "No User ID"
                            }
                            results.add(
                                ReservationWithFacilityInfo(
                                    reservation = reservation,
                                    facilityName = facilityName,
                                    userDisplayID = userDisplayID
                                )
                            )
                        }
                    } catch (e: Exception) {
                        println("❌ Error processing reservation: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // Sort by booked time (newest first)
                _searchResults.value = results.sortedByDescending {
                    it.reservation.bookedTime?.toDate()?.time ?: 0L
                }

                println("✅ Found ${results.size} bookings for facility: $facilityId")
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
                _selectedFacility.value?.let { facility ->
                    searchBookingsByFacility(facility.id)
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