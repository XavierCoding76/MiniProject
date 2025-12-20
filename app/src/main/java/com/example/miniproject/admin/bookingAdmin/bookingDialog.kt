package com.example.miniproject.admin.bookingAdmin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
            ) { selectedTime, facilityDetails, selectedDate, bookedHours ->
                validateReservationTime(selectedTime, facilityDetails, selectedDate, bookedHours)
                validatePastTime(selectedTime, selectedDate)
            }.collect()
        }

        // Validate arena access when displayId or facility changes
        viewModelScope.launch {
            combine(
                _displayId,
                _selectedFacilityDetails
            ) { displayId, facilityDetails ->
                validateArenaAccess(displayId, facilityDetails)
            }.collect()
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

                // Re-validate arena access after display ID validation
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

        // Check if facility is AL (Arena for Lecturers)
        if (facilityDetails.id.startsWith("AL", ignoreCase = true)) {
            // Display ID must start with 'S' for staff
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

        // Create calendar for selected date/time
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.timeInMillis = selectedDate
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
        selectedCalendar.set(Calendar.MINUTE, minute)
        selectedCalendar.set(Calendar.SECOND, 0)
        selectedCalendar.set(Calendar.MILLISECOND, 0)

        // Get current time
        val currentCalendar = Calendar.getInstance()

        // Compare selected time with current time
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
        bookedHours: Double  // Changed from Int to Double
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

        val reservationEndMinutes = selectedTotalMinutes + (bookedHours * 60).toInt()  // Convert to Int for calculation
        if (reservationEndMinutes > endTotalMinutes) {
            _timeValidationError.value = "Reservation exceeds facility closing time (${formatTime(facilityEndTime.first, facilityEndTime.second)})"
            return
        }

        if (reservationEndMinutes >= 1440) { // 24 * 60
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
                val snapshot = db.collection("facilityind").get().await()
                val facilityList = snapshot.documents.mapNotNull { doc ->
                    Facility(
                        id = doc.id,
                        name = doc.getString("name") ?: doc.id
                    )
                }
                _facilities.value = facilityList.sortedBy { it.name }
                println("✅ Loaded ${facilityList.size} facilities")
            } catch (e: Exception) {
                _error.value = "Failed to load facilities: ${e.message}"
                println("❌ Error loading facilities: ${e.message}")
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
                    val details = FacilityDetails(
                        id = facilityDoc.id,
                        name = facilityDoc.getString("name") ?: facilityDoc.id,
                        startTime = facilityDoc.getString("startTime") ?: "0900",
                        endTime = facilityDoc.getString("endTime") ?: "1700"
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
            null
        }
    }

    private fun extractParentFacilityId(facilityIndId: String): String {
        return if ("_" in facilityIndId) {
            facilityIndId.substringBefore("_")
        } else {
            facilityIndId
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
            // Re-validate arena access when facility changes
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

    fun clearError() {
        _error.value = null
        _timeValidationError.value = null
        _arenaAccessError.value = null
        _pastTimeError.value = null
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

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = _selectedDate.value!!
                val (hour, minute) = _selectedTime.value!!
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val bookedTime = Timestamp(calendar.time)

                val reservation = hashMapOf(
                    "id" to _reservationId.value,
                    "userID" to _displayId.value,
                    "facilityID" to _selectedFacility.value!!.id,
                    "bookedTime" to bookedTime,
                    "bookedHours" to _bookedHours.value  // Already a Double
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReservationScreen(
    navController: NavController,
    reservationId: String? = null,
    viewModel: AddEditReservationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val reservationIdState by viewModel.reservationId.collectAsState()
    val displayId by viewModel.displayId.collectAsState()
    val displayIdValid by viewModel.displayIdValid.collectAsState()
    val displayIdChecking by viewModel.displayIdChecking.collectAsState()
    val selectedFacility by viewModel.selectedFacility.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val selectedFacilityDetails by viewModel.selectedFacilityDetails.collectAsState()
    val bookedHours by viewModel.bookedHours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val timeValidationError by viewModel.timeValidationError.collectAsState()
    val arenaAccessError by viewModel.arenaAccessError.collectAsState()
    val pastTimeError by viewModel.pastTimeError.collectAsState()

    var showFacilityDropdown by remember { mutableStateOf(false) }

    val isSaveEnabled = remember {
        derivedStateOf {
            displayId.isNotBlank() &&
                    displayIdValid &&
                    selectedFacility != null &&
                    viewModel.selectedDate.value != null &&
                    viewModel.selectedTime.value != null &&
                    timeValidationError == null &&
                    arenaAccessError == null &&
                    pastTimeError == null
        }
    }

    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        }
    }

    LaunchedEffect(success) {
        if (success) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF483D8B), Color(0xFF6A5ACD))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            error!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = reservationIdState,
                onValueChange = { },
                label = { Text("Reservation ID") },
                leadingIcon = {
                    Icon(Icons.Filled.Badge, contentDescription = "ID")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                OutlinedTextField(
                    value = displayId,
                    onValueChange = { viewModel.setDisplayId(it) },
                    label = { Text("User Display ID") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "User")
                    },
                    trailingIcon = {
                        if (displayIdChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else if (displayId.isNotBlank()) {
                            Icon(
                                imageVector = if (displayIdValid) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = if (displayIdValid) "Valid" else "Invalid",
                                tint = if (displayIdValid) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = displayId.isNotBlank() && !displayIdValid && !displayIdChecking
                )

                if (displayId.isNotBlank() && !displayIdValid && !displayIdChecking) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Display ID not found in user collection",
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                } else if (displayId.isNotBlank() && displayIdValid) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Valid",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "✓ Valid Display ID",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp
                        )
                    }
                }

                if (displayId.isEmpty()) {
                    Text(
                        "Enter the user's Display ID (not the document ID)",
                        color = Color(0xFF757575),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                ExposedDropdownMenuBox(
                    expanded = showFacilityDropdown,
                    onExpandedChange = { showFacilityDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedFacility?.name ?: "Select Facility",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Facility") },
                        leadingIcon = {
                            Icon(Icons.Filled.LocationOn, contentDescription = "Facility")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFacilityDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showFacilityDropdown,
                        onDismissRequest = { showFacilityDropdown = false }
                    ) {
                        facilities.forEach { facility ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(facility.name)
                                        val parentId = if ("_" in facility.id) {
                                            facility.id.substringBefore("_")
                                        } else {
                                            facility.id
                                        }
                                        val location = when {
                                            parentId.startsWith(
                                                "S",
                                                ignoreCase = true
                                            ) -> "Sports Complex"

                                            parentId.startsWith("CC", ignoreCase = true) -> "CITC"
                                            parentId.startsWith("L", ignoreCase = true) -> "Library"
                                            parentId.startsWith("AL", ignoreCase = true) ||
                                                    parentId.startsWith(
                                                        "AS",
                                                        ignoreCase = true
                                                    ) -> "Arena TARUMT"

                                            parentId.startsWith(
                                                "C",
                                                ignoreCase = true
                                            ) -> "Clubhouse"

                                            else -> "Unknown Location"
                                        }
                                        Text(
                                            text = "Location: $location",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setSelectedFacility(facility)
                                    showFacilityDropdown = false
                                }
                            )
                        }
                    }
                }

                if (selectedFacilityDetails != null) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                        val location = when {
                            selectedFacilityDetails!!.id.startsWith(
                                "S",
                                ignoreCase = true
                            ) -> "Sports Complex"

                            selectedFacilityDetails!!.id.startsWith(
                                "CC",
                                ignoreCase = true
                            ) -> "CITC"

                            selectedFacilityDetails!!.id.startsWith(
                                "L",
                                ignoreCase = true
                            ) -> "Library"

                            selectedFacilityDetails!!.id.startsWith("AL", ignoreCase = true) ||
                                    selectedFacilityDetails!!.id.startsWith(
                                        "AS",
                                        ignoreCase = true
                                    ) -> "Arena TARUMT"

                            selectedFacilityDetails!!.id.startsWith(
                                "C",
                                ignoreCase = true
                            ) -> "Clubhouse"

                            else -> "Unknown Location"
                        }
                        Text(
                            text = "Location: $location",
                            fontSize = 12.sp,
                            color = Color(0xFF483D8B),
                            fontWeight = FontWeight.Medium
                        )
                        val hours = viewModel.getFacilityHours()
                        if (hours.isNotEmpty()) {
                            Text(
                                text = hours,
                                fontSize = 12.sp,
                                color = Color(0xFF483D8B)
                            )
                        }
                    }
                }

                // Arena access error warning
                if (arenaAccessError != null) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Arena Access Error",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            arenaAccessError!!,
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.getFormattedDate(),
                onValueChange = { },
                label = { Text("Date") },
                leadingIcon = {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Date")
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance()
                                    selectedCalendar.set(year, month, dayOfMonth)
                                    viewModel.setSelectedDate(selectedCalendar.timeInMillis)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                OutlinedTextField(
                    value = viewModel.getFormattedTime(),
                    onValueChange = { },
                    label = { Text("Start Time") },
                    leadingIcon = {
                        Icon(Icons.Filled.AccessTime, contentDescription = "Time")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        viewModel.setSelectedTime(hourOfDay, minute)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Pick Time")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = timeValidationError != null || pastTimeError != null
                )

                // Display past time error (priority)
                if (pastTimeError != null) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Past Time Error",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            pastTimeError!!,
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                }

                // Display facility hours validation error (only if no past time error)
                if (timeValidationError != null && pastTimeError == null) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Time Error",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            timeValidationError!!,
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                }

                val endTime = viewModel.getReservationEndTime()
                if (endTime.isNotEmpty()) {
                    Text(
                        text = "Reservation End Time: $endTime",
                        fontSize = 12.sp,
                        color = if (timeValidationError == null && pastTimeError == null)
                            Color(0xFF483D8B) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Booked Hours",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF483D8B)
            )
            Spacer(modifier = Modifier.height(8.dp))

// First row: 0.5, 1, 1.5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(0.5, 1.0, 1.5).forEach { hours ->
                    FilterChip(
                        selected = bookedHours == hours,
                        onClick = { viewModel.setBookedHours(hours) },
                        label = {
                            Text(
                                if (hours == 0.5 || hours == 1.5 || hours == 2.5) {
                                    "${
                                        hours.toString().replace(".0", "")
                                    } Hour${if (hours > 1) "s" else ""}"
                                } else {
                                    "${hours.toInt()} Hour${if (hours > 1) "s" else ""}"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

// Second row: 2, 2.5, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(2.0, 2.5, 3.0).forEach { hours ->
                    FilterChip(
                        selected = bookedHours == hours,
                        onClick = { viewModel.setBookedHours(hours) },
                        label = {
                            Text(
                                if (hours == 0.5 || hours == 1.5 || hours == 2.5) {
                                    "${hours} Hours"
                                } else {
                                    "${hours.toInt()} Hours"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}