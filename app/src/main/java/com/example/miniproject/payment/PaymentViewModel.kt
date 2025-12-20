package com.example.miniproject.payment

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class BookingResult {
    object Idle : BookingResult()
    object Loading : BookingResult()
    data class Success(
        val paymentId: String,
        val reservationId: String,
        val captureId: String
    ) : BookingResult()
    data class Error(val message: String) : BookingResult()
}

data class EquipmentItem(
    val id: String,
    val name: String,
    val unitPrice: Double,
    val purchaseQuantity: Int,
    val facilityID: String = "",
    val userId: String = ""
)

class PaymentViewModel : ViewModel() {
    // State variables
    var facility by mutableStateOf<Map<String, Any>?>(null)
    var facilityInd by mutableStateOf<Map<String, Any>?>(null)
    var equipmentItems by mutableStateOf<List<EquipmentItem>>(emptyList())
    var startTime by mutableStateOf<Timestamp?>(null)
    var bookedHours by mutableStateOf(1.0)  // CHANGED: Store hours instead of endTime
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    // CALCULATED property for endTime
    val endTime: Timestamp?
        get() = startTime?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it.toDate()

            // Convert hours to minutes for precision
            val totalMinutes = (bookedHours * 60).toInt()
            calendar.add(Calendar.MINUTE, totalMinutes)

            Timestamp(calendar.time)
        }

    private val _bookingResult = mutableStateOf<BookingResult>(BookingResult.Idle)
    val bookingResult: State<BookingResult> = _bookingResult

    private var currentUserId: String = ""
    private var facilityIndIdCache: String = ""

    fun setUserId(userId: String) {
        currentUserId = userId
    }

    /**
     * Load payment data from Firebase
     */
    fun loadPaymentData(
        userId: String = "",
        facilityIndId: String = "",
        equipmentData: List<String> = emptyList(),
        startTime: Timestamp? = null,
        bookedHours: Double = 1.0,  // CHANGED: Accept bookedHours instead of endTime
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            facilityIndIdCache = facilityIndId
            currentUserId = userId
            println("🔄 Loading booking data...")
            println("   Facility: $facilityIndId")
            println("   Equipment items: $equipmentData")
            println("   Start time: $startTime")
            println("   Booked hours: $bookedHours")

            try {
                if (facilityIndId.isEmpty()) {
                    errorMessage = "No facility selected. Please select a facility."
                    isLoading = false
                    return@launch
                }

                val db = Firebase.firestore

                // 1. Load child facility (facilityind) - specific room
                println("1️⃣ Loading room details: $facilityIndId")
                try {
                    val facilityIndDoc = db.collection("facilityind")
                        .document(facilityIndId)
                        .get()
                        .await()

                    if (facilityIndDoc.exists()) {
                        facilityInd = facilityIndDoc.data
                        println("✅ Room loaded: ${facilityInd?.get("name")}")
                    } else {
                        errorMessage = "Room not found: $facilityIndId"
                        isLoading = false
                        return@launch
                    }

                    // 2. Extract and load parent facility (facility) - facility type
                    val parentFacilityId = extractParentFacilityId(facilityIndId)
                    println("2️⃣ Loading facility type: $parentFacilityId")

                    if (parentFacilityId != null) {
                        val facilityDoc = db.collection("facility")
                            .document(parentFacilityId)
                            .get()
                            .await()

                        if (facilityDoc.exists()) {
                            facility = facilityDoc.data
                            println("✅ Facility type loaded: ${facility?.get("name")}")
                        }
                    }
                } catch (e: Exception) {
                    errorMessage = "Error loading facility: ${e.message}"
                    println("❌ Error: ${e.message}")
                }

                // 3. Load equipment items
                if (equipmentData.isNotEmpty()) {
                    println("3️⃣ Loading equipment: ${equipmentData.size} items")
                    val items = mutableListOf<EquipmentItem>()

                    equipmentData.forEach { data ->
                        val parts = data.split(":")

                        if (parts.size == 2) {
                            val equipmentId = parts[0].trim()
                            val purchaseQuantity = parts[1].toIntOrNull() ?: 1

                            try {
                                val equipmentDoc = db.collection("equipment")
                                    .document(equipmentId)
                                    .get()
                                    .await()

                                if (equipmentDoc.exists()) {
                                    val equipment = equipmentDoc.data ?: emptyMap()
                                    val name = equipment["name"] as? String ?: "Unknown Equipment"

                                    val priceValue = equipment["price"]
                                    val unitPrice = when (priceValue) {
                                        is String -> priceValue.toDoubleOrNull() ?: 0.0
                                        is Number -> priceValue.toDouble()
                                        else -> 0.0
                                    }

                                    items.add(
                                        EquipmentItem(
                                            id = equipmentId,
                                            name = name,
                                            unitPrice = unitPrice,
                                            purchaseQuantity = purchaseQuantity,
                                            facilityID = ""
                                        )
                                    )

                                    println("   ✅ Equipment: $name x$purchaseQuantity")
                                }
                            } catch (e: Exception) {
                                println("   ⚠️ Error loading equipment: ${e.message}")
                            }
                        }
                    }

                    equipmentItems = items
                }

                // 4. Set booking times
                this@PaymentViewModel.startTime = startTime
                this@PaymentViewModel.bookedHours = bookedHours  // CHANGED

                println("📊 Summary:")
                println("   Room: ${getFacilityIndName()}")
                println("   Facility Type: ${getFacilityName()}")
                println("   Equipment: ${equipmentItems.size} items")
                println("   Duration: $bookedHours hours")
                println("   Total: RM ${"%.2f".format(calculateTotal())}")

            } catch (e: Exception) {
                errorMessage = "Failed to load booking data: ${e.message}"
                println("❌ Error: ${e.message}")
            } finally {
                isLoading = false
                println("✅ Data loading completed")
            }
        }
    }

    private fun extractParentFacilityId(facilityIndId: String): String? {
        return try {
            if (facilityIndId.contains("_")) {
                facilityIndId.split("_")[0]
            } else {
                facilityIndId
            }
        } catch (e: Exception) {
            null
        }
    }

    fun calculateTotal(): Double {
        var total = 0.0
        total += getFacilityPrice()
        equipmentItems.forEach { item ->
            total += item.unitPrice * item.purchaseQuantity
        }
        return total
    }

    fun getFacilityPrice(): Double {
        val childPriceValue = facilityInd?.get("price")
        val childPrice = when (childPriceValue) {
            is String -> childPriceValue.toDoubleOrNull()
            is Number -> childPriceValue.toDouble()
            else -> null
        }

        if (childPrice != null && childPrice > 0.0) {
            return childPrice
        }

        val parentPriceValue = facility?.get("price")
        return when (parentPriceValue) {
            is String -> parentPriceValue.toDoubleOrNull() ?: 0.0
            is Number -> parentPriceValue.toDouble()
            else -> 0.0
        }
    }

    fun getFacilityCapacity(): String {
        val childMaxNum = facilityInd?.get("customMaxNum")
        val childMinNum = facilityInd?.get("customMinNum")

        val customMaxNum = when (childMaxNum) {
            is String -> childMaxNum.toIntOrNull()
            is Number -> childMaxNum.toInt()
            else -> null
        }

        val customMinNum = when (childMinNum) {
            is String -> childMinNum.toIntOrNull()
            is Number -> childMinNum.toInt()
            else -> null
        }

        if (customMaxNum != null && customMaxNum > 0) {
            return if (customMinNum != null && customMinNum > 0) {
                "$customMinNum - $customMaxNum people"
            } else {
                "Up to $customMaxNum people"
            }
        }

        val parentMaxNum = facility?.get("maxNum")
        val parentMinNum = facility?.get("minNum")

        val defaultMaxNum = when (parentMaxNum) {
            is String -> parentMaxNum.toIntOrNull()
            is Number -> parentMaxNum.toInt()
            else -> null
        }

        val defaultMinNum = when (parentMinNum) {
            is String -> parentMinNum.toIntOrNull()
            is Number -> parentMinNum.toInt()
            else -> null
        }

        return if (defaultMaxNum != null && defaultMinNum != null) {
            "$defaultMinNum - $defaultMaxNum people"
        } else if (defaultMaxNum != null) {
            "Up to $defaultMaxNum people"
        } else if (defaultMinNum != null) {
            "Minimum $defaultMinNum people"
        } else {
            ""
        }
    }

    fun getFacilityLocation(): String {
        return facility?.get("location") as? String ?: ""
    }

    fun getFacilityDescription(): String {
        return facility?.get("description") as? String ?: ""
    }

    fun getFacilityName(): String {
        return facility?.get("name") as? String ?: "Unknown Facility"
    }

    fun getFacilityIndName(): String {
        return facilityInd?.get("name") as? String ?: "Unknown Room"
    }

    fun clearData() {
        facility = null
        facilityInd = null
        equipmentItems = emptyList()
        startTime = null
        bookedHours = 1.0  // CHANGED
        errorMessage = null
        isLoading = true
        facilityIndIdCache = ""
    }

    /**
     * Check if payment is required (total > 0)
     */
    fun isPaymentRequired(): Boolean {
        return calculateTotal() > 0.0
    }

    /**
     * Create booking for free reservations (no payment needed)
     */
    fun createFreeBooking(
        onSuccess: (reservationId: String) -> Unit,
        onError: (String) -> Unit,
        userId: String
    ) {
        viewModelScope.launch {
            _bookingResult.value = BookingResult.Loading

            try {
                val facilityIndId = facilityIndIdCache
                if (facilityIndId.isEmpty()) {
                    _bookingResult.value = BookingResult.Error("No facility selected")
                    onError("No facility selected")
                    return@launch
                }

                if (startTime == null) {
                    _bookingResult.value = BookingResult.Error("No booking time selected")
                    onError("No booking time selected")
                    return@launch
                }

                if (currentUserId.isEmpty()) {
                    _bookingResult.value = BookingResult.Error("User not logged in")
                    onError("User not logged in")
                    return@launch
                }

                // Generate reservation ID only
                val reservationId = generateNextReservationId()

                // Save only reservation (no payment)
                val success = saveFreeReservationToFirestore(
                    reservationId = reservationId,
                    facilityIndId = facilityIndId,
                    userId = currentUserId
                )

                if (success) {
                    _bookingResult.value = BookingResult.Success(
                        paymentId = "",
                        reservationId = reservationId,
                        captureId = ""
                    )
                    onSuccess(reservationId)
                } else {
                    val errorMsg = "Failed to save reservation to database"
                    _bookingResult.value = BookingResult.Error(errorMsg)
                    onError(errorMsg)
                }

            } catch (e: Exception) {
                val errorMsg = "Error creating reservation: ${e.message}"
                _bookingResult.value = BookingResult.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    /**
     * Create complete booking after successful PayPal payment
     */
    fun createBookingAfterPayment(
        paypalCaptureId: String,
        onSuccess: (paymentId: String, reservationId: String) -> Unit,
        onError: (String) -> Unit,
        userId: String
    ) {
        viewModelScope.launch {
            _bookingResult.value = BookingResult.Loading

            try {
                if (userId.isNotEmpty()) {
                    currentUserId = userId
                    println("✅ User ID set: $userId")
                }
                val facilityIndId = facilityIndIdCache
                if (facilityIndId.isEmpty()) {
                    _bookingResult.value = BookingResult.Error("No facility selected")
                    onError("No facility selected")
                    return@launch
                }

                if (startTime == null) {
                    _bookingResult.value = BookingResult.Error("No booking time selected")
                    onError("No booking time selected")
                    return@launch
                }

                if (currentUserId.isEmpty()) {
                    _bookingResult.value = BookingResult.Error("User not logged in")
                    onError("User not logged in")
                    return@launch
                }

                // Check if payment is actually required
                val totalAmount = calculateTotal()
                if (totalAmount <= 0.0) {
                    _bookingResult.value = BookingResult.Error("No payment required for free bookings")
                    onError("Use createFreeBooking() for free reservations")
                    return@launch
                }

                // Generate IDs
                val paymentId = generateNextPaymentId()
                val reservationId = generateNextReservationId()

                // Create and save all documents
                val success = saveBookingToFirestore(
                    paymentId = paymentId,
                    reservationId = reservationId,
                    paypalCaptureId = paypalCaptureId,
                    facilityIndId = facilityIndId,
                    userId = currentUserId,
                    totalAmount = totalAmount
                )

                if (success) {
                    _bookingResult.value = BookingResult.Success(
                        paymentId = paymentId,
                        reservationId = reservationId,
                        captureId = paypalCaptureId
                    )
                    onSuccess(paymentId, reservationId)
                } else {
                    val errorMsg = "Failed to save booking to database"
                    _bookingResult.value = BookingResult.Error(errorMsg)
                    onError(errorMsg)
                }

            } catch (e: Exception) {
                val errorMsg = "Error creating booking: ${e.message}"
                _bookingResult.value = BookingResult.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    private suspend fun generateNextPaymentId(): String {
        val db = Firebase.firestore
        return try {
            val payments = db.collection("payment")
                .get()
                .await()

            var maxNumber = 0
            payments.documents.forEach { document ->
                val docId = document.id
                if (docId.startsWith("P")) {
                    val numberStr = docId.substring(1)
                    val number = numberStr.toIntOrNull() ?: 0
                    if (number > maxNumber) {
                        maxNumber = number
                    }
                }
            }

            "P${maxNumber + 1}"
        } catch (e: Exception) {
            "P${System.currentTimeMillis()}"
        }
    }

    private suspend fun generateNextReservationId(): String {
        val db = Firebase.firestore
        return try {
            val reservations = db.collection("reservation")
                .get()
                .await()

            var maxNumber = 0
            reservations.documents.forEach { document ->
                val docId = document.id
                if (docId.startsWith("R")) {
                    val numberStr = docId.substring(1)
                    val number = numberStr.toIntOrNull() ?: 0
                    if (number > maxNumber) {
                        maxNumber = number
                    }
                }
            }

            "R${maxNumber + 1}"
        } catch (e: Exception) {
            "R${System.currentTimeMillis()}"
        }
    }

    /**
     * Save only reservation for free bookings (no payment)
     */
    private suspend fun saveFreeReservationToFirestore(
        reservationId: String,
        facilityIndId: String,
        userId: String
    ): Boolean {
        return try {
            val db = Firebase.firestore

            // Create Reservation only
            val reservation = hashMapOf(
                "id" to reservationId,
                "bookedTime" to startTime,
                "facilityID" to facilityIndId,
                "userID" to userId,
                "bookedHours" to bookedHours  // CHANGED: Store bookedHours
            )

            // Save reservation
            val reservationRef = db.collection("reservation").document(reservationId)
            reservationRef.set(reservation).await()

            println("✅ Free reservation saved successfully")
            println("   Reservation ID: $reservationId")
            println("   Facility: $facilityIndId")
            println("   User: $userId")
            println("   Duration: $bookedHours hours")
            println("   No payment required (RM 0.00)")

            true

        } catch (e: Exception) {
            println("❌ Error saving free reservation: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Save all documents to Firestore
     */
    private suspend fun saveBookingToFirestore(
        paymentId: String,
        reservationId: String,
        paypalCaptureId: String,
        facilityIndId: String,
        userId: String,
        totalAmount: Double
    ): Boolean {
        return try {
            val db = Firebase.firestore

            // 1. Create Reservation - matches your Reservation data class
            val reservation = hashMapOf(
                "id" to reservationId,
                "bookedTime" to startTime,
                "facilityID" to facilityIndId,
                "userID" to userId,
                "bookedHours" to bookedHours  // CHANGED: Store bookedHours
            )

            // 2. Create Payment - matches your Payment data class
            val payment = hashMapOf(
                "id" to paymentId,
                "date" to Timestamp.now(),
                "totalAmount" to totalAmount,
                "reservationID" to reservationId
            )

            // 3. Create PaymentDetails - matches your PaymentDetail data class
            val paymentDetails = mutableListOf<Map<String, Any>>()

            // Add facility as first payment detail
            paymentDetails.add(
                hashMapOf(
                    "id" to "${paymentId}_1",
                    "paymentId" to paymentId,
                    "equipmentId" to "",  // Empty for facility booking
                    "quantityRented" to 1
                )
            )

            // Add equipment items
            var itemCounter = 2
            equipmentItems.forEach { equipment ->
                paymentDetails.add(
                    hashMapOf(
                        "id" to "${paymentId}_${itemCounter}",
                        "paymentId" to paymentId,
                        "equipmentId" to equipment.id,
                        "quantityRented" to equipment.purchaseQuantity
                    )
                )
                itemCounter++
            }

            // 4. Batch write all documents
            val batch = db.batch()

            // Add reservation
            val reservationRef = db.collection("reservation").document(reservationId)
            batch.set(reservationRef, reservation)

            // Add payment
            val paymentRef = db.collection("payment").document(paymentId)
            batch.set(paymentRef, payment)

            // Add payment details
            paymentDetails.forEach { detail ->
                val detailId = detail["id"] as String
                val detailRef = db.collection("paymentdetail").document(detailId)
                batch.set(detailRef, detail)
            }

            // Commit batch
            batch.commit().await()

            // 5. Update equipment stock (optional)
            updateEquipmentStock()

            println("✅ Booking saved successfully")
            println("   Payment ID: $paymentId")
            println("   Reservation ID: $reservationId")
            println("   PayPal Capture ID: $paypalCaptureId")
            println("   Facility: $facilityIndId")
            println("   Duration: $bookedHours hours")
            println("   Total: RM $totalAmount")
            println("   Items: ${paymentDetails.size} items")

            true

        } catch (e: Exception) {
            println("❌ Error saving booking: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private suspend fun updateEquipmentStock() {
        try {
            val db = Firebase.firestore

            equipmentItems.forEach { equipment ->
                val equipmentRef = db.collection("equipment").document(equipment.id)
                val equipmentDoc = equipmentRef.get().await()

                if (equipmentDoc.exists()) {
                    val stockField = equipmentDoc.get("stock")
                    if (stockField != null) {
                        val currentStock = when (stockField) {
                            is String -> stockField.toIntOrNull() ?: 0
                            is Number -> stockField.toInt()
                            else -> 0
                        }
                        val newStock = currentStock - equipment.purchaseQuantity

                        if (newStock >= 0) {
                            equipmentRef.update("stock", newStock).await()
                            println("✅ Updated stock for ${equipment.name}: $currentStock → $newStock")
                        } else {
                            println("⚠️ Insufficient stock for ${equipment.name}")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            println("⚠️ Warning: Could not update equipment stock: ${e.message}")
        }
    }

    fun resetBookingResult() {
        _bookingResult.value = BookingResult.Idle
    }

    fun getFacilityDisplayName(): String {
        val facilityName = facility?.get("name") as? String ?: "Facility"
        val roomName = facilityInd?.get("name") as? String ?: ""

        return if (roomName.isNotEmpty() && roomName != "Unknown Room") {
            "$facilityName - $roomName"
        } else {
            facilityName
        }
    }

    fun getFormattedDateRange(): String {
        if (startTime == null || endTime == null) return "Date not specified"

        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val startDate = startTime!!.toDate()
        val endDate = endTime!!.toDate()  // Uses calculated endTime

        return if (dateFormat.format(startDate) == dateFormat.format(endDate)) {
            "${dateFormat.format(startDate)} • ${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
        } else {
            "${dateFormat.format(startDate)} ${timeFormat.format(startDate)} - ${dateFormat.format(endDate)} ${timeFormat.format(endDate)}"
        }
    }
}