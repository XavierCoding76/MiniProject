package com.example.miniproject.payment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PaymentViewModel : ViewModel() {

    var payment by mutableStateOf<Map<String, Any>?>(null)
    var paymentDetails by mutableStateOf<List<Map<String, Any>>>(emptyList())
    var reservation by mutableStateOf<Map<String, Any>?>(null)
    var facility by mutableStateOf<Map<String, Any>?>(null)
    var equipmentDetails by mutableStateOf<Map<String, Map<String, Any>>>(emptyMap())
    var isLoading by mutableStateOf(true)

    fun loadPaymentData(paymentId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val db = Firebase.firestore

                // 1. Fetch Payment
                val paymentDoc = db.collection("payment").document(paymentId).get().await()
                payment = paymentDoc.data
                val reservationId = payment?.get("reservationID") as? String

                if (reservationId != null) {
                    // 2. Fetch Reservation
                    val reservationDoc = db.collection("reservations").document(reservationId).get().await()
                    reservation = reservationDoc.data
                    val facilityId = reservation?.get("facilityID") as? String

                    if (facilityId != null) {
                        // 3. Fetch Facility
                        val facilityDoc = db.collection("facility").document(facilityId).get().await()
                        facility = facilityDoc.data
                    }
                }

                // 4. Fetch Payment Details
                val paymentDetailDocs = db.collection("paymentdetail")
                    .whereEqualTo("paymentId", paymentId)
                    .get()
                    .await()
                paymentDetails = paymentDetailDocs.documents.mapNotNull { it.data }

                // 5. Fetch related Equipment for names and prices
                val tempEquipmentDetails = mutableMapOf<String, Map<String, Any>>()
                for (detail in paymentDetails) {
                    val equipmentId = detail["equipmentId"] as? String
                    if (equipmentId != null && !tempEquipmentDetails.containsKey(equipmentId)) {
                        val equipmentDoc = db.collection("equipment").document(equipmentId).get().await()
                        equipmentDoc.data?.let {
                            tempEquipmentDetails[equipmentId] = it
                        }
                    }
                }
                equipmentDetails = tempEquipmentDetails

            } catch (e: Exception) {
                println("Error loading payment data: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}
