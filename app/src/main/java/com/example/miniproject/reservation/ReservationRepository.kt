package com.example.miniproject.reservation

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class ReservationRepository {

    private val reservationsCollection = FirebaseManager.firestore.collection("reservation")

    suspend fun createReservation(reservation: Reservation) {
        reservationsCollection.document(reservation.id).set(reservation).await()
    }

    suspend fun getReservation(id: String): Reservation? {
        val documentSnapshot = reservationsCollection.document(id).get().await()
        return documentSnapshot.toObject<Reservation>()
    }

    suspend fun getAllReservations(): List<Reservation> {
        val querySnapshot = reservationsCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Reservation>() }
    }

    // Simple, direct search for reservations by the userID field.
    suspend fun findReservationsByUserId(userId: String): List<Reservation> {
        if (userId.isBlank()) {
            return emptyList()
        }
        val querySnapshot = reservationsCollection.whereEqualTo("userID", userId).get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Reservation>() }
    }

    // This function is kept for potential future use, but is not used in the simple search.
    suspend fun findReservationsByUserIds(userIds: List<String>): List<Reservation> {
        if (userIds.isEmpty()) return emptyList()
        val querySnapshot = reservationsCollection.whereIn("userID", userIds).get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Reservation>() }
    }
}
