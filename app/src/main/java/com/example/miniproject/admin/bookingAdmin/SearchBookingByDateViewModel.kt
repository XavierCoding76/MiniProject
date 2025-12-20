package com.example.miniproject.admin.bookingAdmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.reservation.Reservation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Data class to pair reservations with facility names AND user display IDs
data class ReservationWithFacility(
    val reservation: Reservation,
    val facilityName: String,
    val userDisplayID: String // Add this field
)

class SearchBookingByDateViewModel : ViewModel() {

    // Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Date formatter for displaying selected date
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    // State matching SearchBookingByUserViewModel pattern
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ReservationWithFacility>?>(null)
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    // Store selected date for internal use
    private var selectedDateMillis: Long? = null

    // Functions matching user search pattern
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun setSelectedDate(millis: Long?) {
        selectedDateMillis = millis
        if (millis != null) {
            val dateStr = dateFormat.format(Date(millis))
            _searchText.value = dateStr
            performSearch(dateStr, millis)
        } else {
            _searchText.value = ""
            _searchResults.value = null
        }
    }

    fun getFormattedDate(): String {
        return selectedDateMillis?.let {
            dateFormat.format(Date(it))
        } ?: ""
    }

    fun addSearchToHistory(search: String) {
        if (search.isNotBlank()) {
            val currentHistory = _searchHistory.value.toMutableList()
            currentHistory.remove(search)
            currentHistory.add(0, search)
            _searchHistory.value = if (currentHistory.size > 10) {
                currentHistory.take(10)
            } else {
                currentHistory
            }
        }
    }

    fun onClearHistoryItem(item: String) {
        _searchHistory.value = _searchHistory.value.filter { it != item }
    }

    fun clearAllHistory() {
        _searchHistory.value = emptyList()
    }

    fun performSearch(searchQuery: String, dateMillis: Long? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val millisToSearch = dateMillis ?: try {
                    dateFormat.parse(searchQuery)?.time
                } catch (e: Exception) {
                    null
                }

                if (millisToSearch == null) {
                    _searchResults.value = emptyList()
                    return@launch
                }

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = millisToSearch

                // Set to start of day (00:00:00.000)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = Timestamp(calendar.time)

                // Set to end of day (23:59:59.999)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = Timestamp(calendar.time)

                println("🔍 Searching reservations from $startOfDay to $endOfDay")

                val snapshot = db.collection("reservation")
                    .whereGreaterThanOrEqualTo("bookedTime", startOfDay)
                    .whereLessThanOrEqualTo("bookedTime", endOfDay)
                    .get()
                    .await()

                println("📊 Found ${snapshot.documents.size} reservations")

                // DEBUG: Print what's in each reservation
                snapshot.documents.forEach { doc ->
                    println("📝 Reservation ${doc.id}: userID='${doc.getString("userID")}', userDisplayID='${doc.getString("userDisplayID")}'")
                }

                val reservationsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        Reservation(
                            id = doc.getString("id") ?: doc.id,
                            userID = doc.getString("userID") ?: "",
                            facilityID = doc.getString("facilityID") ?: "",
                            bookedTime = doc.getTimestamp("bookedTime"),
                            bookedHours = doc.getDouble("bookedHours") ?: 1.0
                        )
                    } catch (e: Exception) {
                        println("❌ Error parsing reservation: ${e.message}")
                        null
                    }
                }

                // Fetch facility names AND user display IDs for all reservations
                val enrichedReservations = reservationsList.map { reservation ->
                    try {
                        // Fetch facility name
                        val facilityDoc = db.collection("facilityind")
                            .document(reservation.facilityID)
                            .get()
                            .await()
                        val facilityName = facilityDoc.getString("name") ?: reservation.facilityID

                        // Fetch user display ID
                        val userDisplayID = try {
                            val storedValue = reservation.userID

                            if (storedValue.isBlank()) {
                                "Unknown User"
                            } else {
                                // FIRST: Check if stored value is already a displayId
                                val displayIdQuery = db.collection("user")
                                    .whereEqualTo("displayId", storedValue)
                                    .limit(1)
                                    .get()
                                    .await()

                                if (!displayIdQuery.isEmpty) {
                                    // storedValue IS already a displayId
                                    storedValue
                                } else {
                                    // storedValue is likely a Firebase document ID
                                    // Try to get the user document
                                    val userDoc = db.collection("user")
                                        .document(storedValue)
                                        .get()
                                        .await()

                                    if (userDoc.exists()) {
                                        // Get displayId from user document
                                        userDoc.getString("displayId") ?: storedValue
                                    } else {
                                        // User not found, try alternative casing
                                        val altQuery = db.collection("user")
                                            .whereEqualTo("displayID", storedValue) // uppercase D
                                            .limit(1)
                                            .get()
                                            .await()

                                        if (!altQuery.isEmpty) {
                                            storedValue
                                        } else {
                                            // Last resort: check reservation for userDisplayID field
                                            val reservationDoc = db.collection("reservation")
                                                .document(reservation.id)
                                                .get()
                                                .await()

                                            reservationDoc.getString("userDisplayID") ?: storedValue
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("❌ Error fetching user displayId for '${reservation.userID}': ${e.message}")
                            reservation.userID // Fallback to whatever is stored
                        }

                        ReservationWithFacility(reservation, facilityName, userDisplayID)
                    } catch (e: Exception) {
                        println("❌ Error enriching reservation: ${e.message}")
                        ReservationWithFacility(reservation, reservation.facilityID, reservation.userID)
                    }
                }

                val sortedReservations = enrichedReservations.sortedBy {
                    it.reservation.bookedTime?.seconds
                }

                _searchResults.value = sortedReservations
                addSearchToHistory(searchQuery)
                println("✅ Successfully loaded ${sortedReservations.size} reservations with display IDs")

            } catch (e: Exception) {
                println("❌ Error fetching reservations: ${e.message}")
                _errorMessage.value = "Failed to fetch reservations: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                delay(50L)
                _isLoading.value = false
            }
        }
    }

    fun deleteReservation(reservationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                println("🗑️ Deleting reservation: $reservationId")

                db.collection("reservation")
                    .document(reservationId)
                    .delete()
                    .await()

                println("✅ Reservation deleted successfully")
                _deleteSuccess.value = true

                // Refresh the search results after deletion
                refreshSearch()

            } catch (e: Exception) {
                println("❌ Error deleting reservation: ${e.message}")
                _errorMessage.value = "Failed to delete reservation: ${e.message}"
            } finally {
                _isLoading.value = false
                // Reset delete success flag after a short delay
                delay(1000L)
                _deleteSuccess.value = false
            }
        }
    }

    fun refreshSearch() {
        selectedDateMillis?.let { millis ->
            val dateStr = dateFormat.format(Date(millis))
            performSearch(dateStr, millis)
        }
    }

    fun clearSearch() {
        _searchText.value = ""
        _searchResults.value = null
        selectedDateMillis = null
    }

    fun performSearchWithCurrentDate() {
        selectedDateMillis?.let { millis ->
            val dateStr = dateFormat.format(Date(millis))
            performSearch(dateStr, millis)
        } ?: run {
            _errorMessage.value = "Please select a date first"
        }
    }

    // Legacy functions for compatibility
    fun setSelectedDateLegacy(millis: Long?) {
        setSelectedDate(millis)
    }

    val reservations = searchResults

    fun refreshReservations() {
        refreshSearch()
    }

    fun clearData() {
        clearSearch()
    }
}