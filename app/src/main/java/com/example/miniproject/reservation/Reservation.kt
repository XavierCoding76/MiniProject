package com.example.miniproject.reservation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Reservation(
    @DocumentId
    val id: String = "",
    val bookedTime: Timestamp = Timestamp.now(), // Corrected to match Firestore field
    val facilityID: String = "",
    // This will store the User's displayId for UI purposes.
    val userID: String = "", 
    val bookedHours: Int = 0
)
