package com.example.miniproject.admin.bookingAdmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Facility(
    val id: String = "",
    val name: String = ""
)

data class FacilityDetails(
    val id: String = "",
    val name: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

class AddEditReservationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    private val _reservationId = MutableStateFlow("")
    val reservationId = _reservationId.asStateFlow()

    private val _displayId = MutableStateFlow("")
    val displayId = _displayId.asStateFlow()

    private val _displayIdValid = MutableStateFlow(false)
    val displayIdValid = _displayIdValid.asStateFlow()

    private val _displayIdChecking = MutableStateFlow(false)
    val displayIdChecking = _displayIdChecking.asStateFlow()

    private val _selectedFacility = MutableStateFlow<Facility?>(null)
    val selectedFacility = _selectedFacility.asStateFlow()

    private val _facilities = MutableStateFlow<List<Facility>>(emptyList())
    val facilities = _facilities.asStateFlow()

    private val _selectedFacilityDetails = MutableStateFlow<FacilityDetails?>(null)
    val selectedFacilityDetails = _selectedFacilityDetails.asStateFlow()

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTime = _selectedTime.asStateFlow()

    private val _bookedHours = MutableStateFlow(1.0)
    val bookedHours = _bookedHours.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _timeValidationError = MutableStateFlow<String?>(null)
    val timeValidationError = _timeValidationError.asStateFlow()

    private val _arenaAccessError = MutableStateFlow<String?>(null)
    val arenaAccessError = _arenaAccessError.asStateFlow()

    private val _pastTimeError = MutableStateFlow<String?>(null)
    val pastTimeError = _pastTimeError.asStateFlow()

    private val _timeConflictError = MutableStateFlow<String?>(null)
    val timeConflictError = _timeConflictError.asStateFlow()

    init {
        loadFacilities()
        generateNewReservationId()

        viewModelScope.launch {
            _displayId
                .debounce(500)
                .distinctUntilChanged()
                .collect { displayId ->
                    validateDisplayId(displayId)
                }
        }

        viewModelScope.launch {
            combine(
                _selectedTime,
                _selectedFacilityDetails,
                _selectedDate,
                _bookedHours
            ) { selectedTime: Pair<Int, Int>?,
                facilityDetails: FacilityDetails?,
                selectedDate: Long?,
                bookedHours: Double ->
                validateReservationTime(selectedTime, facilityDetails, selectedDate, bookedHours)
                validatePastTime(selectedTime, selectedDate)
            }.collect()
        }

        viewModelScope.launch {
            combine(
                _displayId,
                _selectedFacilityDetails
            ) { displayId: String,
                facilityDetails: FacilityDetails? ->
                validateArenaAccess(displayId, facilityDetails)
            }.collect()
        }

        setupTimeConflictChecking()
    }

    private fun setupTimeConflictChecking() {
        viewModelScope.launch {
            _selectedFacility.collect { facility ->
                checkTimeConflictsWithCurrentValues(facility)
            }
        }

        viewModelScope.launch {
            _selectedDate.collect { date ->
                checkTimeConflictsWithCurrentValues(_selectedFacility.value, date)
            }
        }

        viewModelScope.launch {
            _selectedTime.collect { time ->
                checkTimeConflictsWithCurrentValues(_selectedFacility.value, _selectedDate.value, time)
            }
        }

        viewModelScope.launch {
            _bookedHours.collect { hours ->
                checkTimeConflictsWithCurrentValues(
                    _selectedFacility.value,
                    _selectedDate.value,
                    _selectedTime.value,
                    hours
                )
            }
        }
    }

    private fun checkTimeConflictsWithCurrentValues(
        facility: Facility? = _selectedFacility.value,
        date: Long? = _selectedDate.value,
        time: Pair<Int, Int>? = _selectedTime.value,
        hours: Double = _bookedHours.value
    ) {
        val editMode = _isEditMode.value
        val resId = _reservationId.value

        if (facility != null && date != null && time != null && hours > 0) {
            checkTimeConflicts(facility.id, date, time, hours, editMode, resId)
        } else {
            _timeConflictError.value = null
        }
    }

    private fun checkTimeConflicts(
        facilityId: String,
        selectedDate: Long,
        selectedTime: Pair<Int, Int>,
        bookedHours: Double,
        isEditMode: Boolean,
        currentReservationId: String
    ) {
        viewModelScope.launch {
            _timeConflictError.value = null

            try {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                val (selectedHour, selectedMinute) = selectedTime

                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val selectedStartTime = Timestamp(calendar.time)

                val selectedEndMillis = calendar.timeInMillis + (bookedHours * 60 * 60 * 1000).toLong()
                val selectedEndTime = Timestamp(Date(selectedEndMillis))

                println("🔍 Checking time conflicts for facility: $facilityId")
                println("   Selected: ${selectedStartTime.toDate()} to ${selectedEndTime.toDate()}")

                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.timeInMillis = selectedDate

                selectedDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
                selectedDateCalendar.set(Calendar.MINUTE, 0)
                selectedDateCalendar.set(Calendar.SECOND, 0)
                selectedDateCalendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = Timestamp(selectedDateCalendar.time)

                selectedDateCalendar.set(Calendar.HOUR_OF_DAY, 23)
                selectedDateCalendar.set(Calendar.MINUTE, 59)
                selectedDateCalendar.set(Calendar.SECOND, 59)
                selectedDateCalendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = Timestamp(selectedDateCalendar.time)

                val snapshot = db.collection("reservation")
                    .whereEqualTo("facilityID", facilityId)
                    .whereGreaterThanOrEqualTo("bookedTime", startOfDay)
                    .whereLessThanOrEqualTo("bookedTime", endOfDay)
                    .orderBy("bookedTime")
                    .get()
                    .await()

                println("📊 Found ${snapshot.documents.size} reservations for this date and facility")

                for (doc in snapshot.documents) {
                    val existingReservationId = doc.id

                    if (isEditMode && existingReservationId == currentReservationId) {
                        println("   ⏭️ Skipping self-check for: $existingReservationId")
                        continue
                    }

                    val existingBookedTime = doc.getTimestamp("bookedTime")
                    val existingBookedHours = doc.getDouble("bookedHours") ?: 1.0

                    if (existingBookedTime != null) {
                        val existingStartTime = existingBookedTime
                        val existingEndMillis = existingBookedTime.toDate().time +
                                (existingBookedHours * 60 * 60 * 1000).toLong()
                        val existingEndTime = Timestamp(Date(existingEndMillis))

                        println("   🔄 Comparing with existing: $existingReservationId")
                        println("      Existing: ${existingStartTime.toDate()} to ${existingEndTime.toDate()}")

                        val selectedStart = selectedStartTime.toDate().time
                        val selectedEnd = selectedEndTime.toDate().time
                        val existingStart = existingStartTime.toDate().time
                        val existingEnd = existingEndTime.toDate().time

                        val overlapExists = selectedStart < existingEnd && selectedEnd > existingStart

                        if (overlapExists) {
                            val existingDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                .format(existingStartTime.toDate())
                            val existingDuration = "${existingBookedHours}h"

                            _timeConflictError.value =
                                "⚠️ Time conflict with existing reservation!\n" +
                                        "Reservation ID: $existingReservationId\n" +
                                        "Existing booking: $existingDate for $existingDuration\n" +
                                        "Please choose a different time slot."

                            println("❌ Time conflict detected with reservation: $existingReservationId")
                            return@launch
                        }
                    }
                }

                println("✅ No time conflicts found")
                _timeConflictError.value = null

            } catch (e: Exception) {
                println("❌ Error checking time conflicts: ${e.message}")
                e.printStackTrace()

                if (e.message?.contains("index") == true ||
                    e.message?.contains("FAILED_PRECONDITION") == true) {
                    _timeConflictError.value = "Database index not ready. Please create the required index in Firebase Console."
                } else {
                    _timeConflictError.value = "Error checking availability: ${e.message}"
                }
            }
        }
    }

    private fun validateDisplayId(displayId: String) {
        viewModelScope.launch {
            _displayIdChecking.value = true

            if (displayId.isBlank()) {
                _displayIdValid.value = false
                _displayIdChecking.value = false
                return@launch
            }

            try {
                val querySnapshot = db.collection("user")
                    .whereEqualTo("displayId", displayId)
                    .limit(1)
                    .get()
                    .await()

                _displayIdValid.value = querySnapshot.documents.isNotEmpty()
                println("✅ Display ID validation: $displayId - Valid: ${_displayIdValid.value}")

                validateArenaAccess(displayId, _selectedFacilityDetails.value)

            } catch (e: Exception) {
                _displayIdValid.value = false
                _error.value = "Error checking user: ${e.message}"
                println("❌ Display ID validation error: ${e.message}")
            } finally {
                _displayIdChecking.value = false
            }
        }
    }

    private fun validateArenaAccess(displayId: String, facilityDetails: FacilityDetails?) {
        if (displayId.isBlank() || facilityDetails == null) {
            _arenaAccessError.value = null
            return
        }

        if (facilityDetails.id.startsWith("AL", ignoreCase = true)) {
            if (!displayId.startsWith("S", ignoreCase = true)) {
                _arenaAccessError.value = "Arena (AL) is restricted to staff only. Display ID must start with 'S'"
                println("❌ Arena access denied: Display ID $displayId does not start with 'S'")
                return
            }
        }

        _arenaAccessError.value = null
        println("✅ Arena access validated for Display ID: $displayId")
    }

    private fun validatePastTime(selectedTime: Pair<Int, Int>?, selectedDate: Long?) {
        if (selectedTime == null || selectedDate == null) {
            _pastTimeError.value = null
            return
        }

        val (hour, minute) = selectedTime

        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.timeInMillis = selectedDate
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
        selectedCalendar.set(Calendar.MINUTE, minute)
        selectedCalendar.set(Calendar.SECOND, 0)
        selectedCalendar.set(Calendar.MILLISECOND, 0)

        val currentCalendar = Calendar.getInstance()

        if (selectedCalendar.timeInMillis <= currentCalendar.timeInMillis) {
            _pastTimeError.value = "Cannot book a reservation in the past. Please select a future date and time."
            println("❌ Past time selected: ${selectedCalendar.time} is before current time: ${currentCalendar.time}")
        } else {
            _pastTimeError.value = null
            println("✅ Future time validated: ${selectedCalendar.time}")
        }
    }

    private fun validateReservationTime(
        selectedTime: Pair<Int, Int>?,
        facilityDetails: FacilityDetails?,
        selectedDate: Long?,
        bookedHours: Double
    ) {
        if (selectedTime == null || facilityDetails == null || selectedDate == null) {
            _timeValidationError.value = null
            return
        }

        val (hour, minute) = selectedTime

        val facilityStartTime = parseTimeString(facilityDetails.startTime)
        val facilityEndTime = parseTimeString(facilityDetails.endTime)

        if (facilityStartTime == null || facilityEndTime == null) {
            _timeValidationError.value = "Facility operating hours are not properly configured"
            println("❌ Invalid facility times: start=${facilityDetails.startTime}, end=${facilityDetails.endTime}")
            return
        }

        val selectedTotalMinutes = hour * 60 + minute
        val startTotalMinutes = facilityStartTime.first * 60 + facilityStartTime.second
        val endTotalMinutes = facilityEndTime.first * 60 + facilityEndTime.second

        println("🕐 Time validation: selected=$selectedTotalMinutes, start=$startTotalMinutes, end=$endTotalMinutes, hours=$bookedHours")

        if (selectedTotalMinutes < startTotalMinutes) {
            _timeValidationError.value = "Reservation time is before facility opening time (${formatTime(facilityStartTime.first, facilityStartTime.second)})"
            return
        }

        val reservationEndMinutes = selectedTotalMinutes + (bookedHours * 60).toInt()
        if (reservationEndMinutes > endTotalMinutes) {
            _timeValidationError.value = "Reservation exceeds facility closing time (${formatTime(facilityEndTime.first, facilityEndTime.second)})"
            return
        }

        if (reservationEndMinutes >= 1440) {
            _timeValidationError.value = "Reservation cannot extend past midnight"
            return
        }

        _timeValidationError.value = null
        println("✅ Time validation passed")
    }

    private fun parseTimeString(timeString: String): Pair<Int, Int>? {
        return try {
            if (timeString.length == 4) {
                val hour = timeString.substring(0, 2).toInt()
                val minute = timeString.substring(2, 4).toInt()
                if (hour in 0..23 && minute in 0..59) {
                    Pair(hour, minute)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ Error parsing time string: $timeString - ${e.message}")
            null
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun loadFacilities() {
        viewModelScope.launch {
            try {
                // Load ALL documents from facilityind collection
                val snapshot = db.collection("facilityind").get().await()

                val facilityList = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: id

                    Facility(
                        id = id,
                        name = "$name ($id)"
                    )
                }

                _facilities.value = facilityList.sortedBy { it.name }
                println("✅ Loaded ${facilityList.size} facilities from facilityind")

                facilityList.forEach { facility ->
                    println("   - ${facility.id}: ${facility.name}")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load facilities: ${e.message}"
                println("❌ Error loading facilities: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadParentFacilityDetails(facilityId: String): FacilityDetails? {
        return try {
            val parentFacilityId = extractParentFacilityId(facilityId)
            println("🔍 Loading parent facility: $parentFacilityId for child: $facilityId")

            if (parentFacilityId.isNotBlank()) {
                val facilityDoc = db.collection("facility")
                    .document(parentFacilityId)
                    .get()
                    .await()

                if (facilityDoc.exists()) {
                    val startTime = facilityDoc.getString("startTime") ?: "0000"
                    val endTime = facilityDoc.getString("endTime") ?: "2359"

                    val details = FacilityDetails(
                        id = facilityDoc.id,
                        name = facilityDoc.getString("name") ?: facilityDoc.id,
                        startTime = startTime,
                        endTime = endTime
                    )
                    println("✅ Loaded facility details: ${details.name}, hours: ${details.startTime}-${details.endTime}")
                    details
                } else {
                    println("❌ Parent facility document not found: $parentFacilityId")
                    null
                }
            } else {
                println("❌ Could not extract parent ID from: $facilityId")
                null
            }
        } catch (e: Exception) {
            println("❌ Error loading parent facility details: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun extractParentFacilityId(facilityIndId: String): String {
        // Handle different facility ID patterns:
        // - "S1_1" -> "S1" (Sports facility 1, court 1)
        // - "AL_2" -> "AL" (Arena/Lecture hall 2)
        // - "CC_1" -> "CC" (Computer center 1)
        // - "L1_3" -> "L1" (Library 1, room 3)

        return when {
            facilityIndId.contains("_") -> {
                // Extract everything before underscore as parent ID
                // "S1_1" -> "S1", "AL_2" -> "AL", "CC_1" -> "CC"
                facilityIndId.substringBefore("_")
            }
            else -> {
                println("⚠️ Unexpected facility ID format (no underscore): $facilityIndId")
                facilityIndId
            }
        }
    }

    private fun generateNewReservationId() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("reservation").get().await()

                val maxNumber = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    if (id.startsWith("R")) {
                        id.substring(1).toIntOrNull()
                    } else null
                }.maxOrNull() ?: 0

                _reservationId.value = "R${maxNumber + 1}"
                println("✅ Generated reservation ID: ${_reservationId.value}")
            } catch (e: Exception) {
                _error.value = "Failed to generate ID: ${e.message}"
                _reservationId.value = "R1"
                println("❌ Error generating ID: ${e.message}")
            }
        }
    }

    fun setDisplayId(displayId: String) {
        _displayId.value = displayId
    }

    fun setSelectedFacility(facility: Facility) {
        viewModelScope.launch {
            _selectedFacility.value = facility
            _selectedFacilityDetails.value = loadParentFacilityDetails(facility.id)
            validateArenaAccess(_displayId.value, _selectedFacilityDetails.value)
        }
    }

    fun setSelectedDate(millis: Long) {
        _selectedDate.value = millis
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        _selectedTime.value = Pair(hour, minute)
        println("🕐 Time set to: ${formatTime(hour, minute)}")
    }

    fun setBookedHours(hours: Double) {
        _bookedHours.value = hours
        validateReservationTime(_selectedTime.value, _selectedFacilityDetails.value, _selectedDate.value, hours)
    }

    fun getFormattedDate(): String {
        return _selectedDate.value?.let { dateFormat.format(Date(it)) } ?: "Select Date"
    }

    fun getFormattedTime(): String {
        return _selectedTime.value?.let { (hour, minute) ->
            formatTime(hour, minute)
        } ?: "Select Time"
    }

    fun getFacilityHours(): String {
        return _selectedFacilityDetails.value?.let { facilityDetails ->
            val startTime = parseTimeString(facilityDetails.startTime)
            val endTime = parseTimeString(facilityDetails.endTime)
            if (startTime != null && endTime != null) {
                "Operating Hours: ${formatTime(startTime.first, startTime.second)} - ${formatTime(endTime.first, endTime.second)}"
            } else {
                "Operating hours not available"
            }
        } ?: ""
    }

    fun getReservationEndTime(): String {
        return _selectedTime.value?.let { (hour, minute) ->
            val totalMinutes = hour * 60 + minute + (_bookedHours.value * 60).toInt()
            val endHour = (totalMinutes / 60) % 24
            val endMinute = totalMinutes % 60
            formatTime(endHour, endMinute)
        } ?: ""
    }

    fun getOperatingHoursMessage(): String {
        return _selectedFacilityDetails.value?.let { details ->
            val start = parseTimeString(details.startTime)
            val end = parseTimeString(details.endTime)
            if (start != null && end != null) {
                "Operating hours: ${formatTime(start.first, start.second)} - ${formatTime(end.first, end.second)}"
            } else {
                "Operating hours not available"
            }
        } ?: "Please select a facility first"
    }

    fun clearError() {
        _error.value = null
        _timeValidationError.value = null
        _arenaAccessError.value = null
        _pastTimeError.value = null
        _timeConflictError.value = null
    }

    fun saveReservation() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (_displayId.value.isBlank()) {
                    _error.value = "Display ID is required"
                    return@launch
                }

                if (!_displayIdValid.value) {
                    _error.value = "Invalid Display ID. Please enter a valid display ID."
                    return@launch
                }

                if (_selectedFacility.value == null) {
                    _error.value = "Please select a facility"
                    return@launch
                }
                if (_selectedDate.value == null) {
                    _error.value = "Please select a date"
                    return@launch
                }
                if (_selectedTime.value == null) {
                    _error.value = "Please select a time"
                    return@launch
                }

                if (_timeValidationError.value != null) {
                    _error.value = _timeValidationError.value
                    return@launch
                }

                if (_arenaAccessError.value != null) {
                    _error.value = _arenaAccessError.value
                    return@launch
                }

                if (_pastTimeError.value != null) {
                    _error.value = _pastTimeError.value
                    return@launch
                }

                if (_timeConflictError.value != null) {
                    _error.value = _timeConflictError.value
                    return@launch
                }

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = _selectedDate.value!!
                val (hour, minute) = _selectedTime.value!!
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val bookedTime = Timestamp(calendar.time)

                val reservation = hashMapOf(
                    "userID" to _displayId.value,
                    "facilityID" to _selectedFacility.value!!.id,
                    "bookedTime" to bookedTime,
                    "bookedHours" to _bookedHours.value
                )

                db.collection("reservation")
                    .document(_reservationId.value)
                    .set(reservation)
                    .await()

                _success.value = true
                println("✅ Reservation saved: ${_reservationId.value}, User: ${_displayId.value}, Facility: ${_selectedFacility.value!!.id}")

            } catch (e: Exception) {
                _error.value = "Failed to save reservation: ${e.message}"
                println("❌ Error saving reservation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReservation(reservationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isEditMode.value = true

            try {
                val doc = db.collection("reservation")
                    .document(reservationId)
                    .get()
                    .await()

                if (doc.exists()) {
                    _reservationId.value = doc.id

                    val displayId = doc.getString("userID") ?: ""
                    _displayId.value = displayId
                    validateDisplayId(displayId)

                    val facilityId = doc.getString("facilityID") ?: ""
                    val facility = _facilities.value.find { it.id == facilityId }
                    _selectedFacility.value = facility

                    if (facility != null) {
                        _selectedFacilityDetails.value = loadParentFacilityDetails(facility.id)
                    }

                    val timestamp = doc.getTimestamp("bookedTime")
                    if (timestamp != null) {
                        val date = timestamp.toDate()
                        val calendar = Calendar.getInstance()
                        calendar.time = date

                        _selectedDate.value = calendar.timeInMillis
                        _selectedTime.value = Pair(
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE)
                        )
                    }

                    _bookedHours.value = doc.getDouble("bookedHours") ?: 1.0
                    println("✅ Loaded reservation: $reservationId")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load reservation: ${e.message}"
                println("❌ Error loading reservation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}