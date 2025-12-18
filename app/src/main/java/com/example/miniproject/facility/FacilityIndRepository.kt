package com.example.miniproject.facility

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FacilityIndRepository {
    private val db = Firebase.firestore
    private val facilityIndCollection = db.collection("facilityind")

    // Get all FacilityInd documents
    suspend fun getAllFacilityInds(): List<FacilityInd> {
        return try {
            facilityIndCollection.get().await().toObjects(FacilityInd::class.java)
        } catch (e: Exception) {
            println("Error fetching all facility inds: ${e.message}")
            emptyList()
        }
    }

    // Get FacilityInd by ID
    suspend fun getFacilityIndById(id: String): FacilityInd? {
        return try {
            facilityIndCollection.document(id).get().await().toObject(FacilityInd::class.java)
        } catch (e: Exception) {
            println("Error fetching facility ind by id: ${e.message}")
            null
        }
    }

    // Get FacilityInd by name
    suspend fun getFacilityIndByName(name: String): FacilityInd? {
        return try {
            facilityIndCollection.whereEqualTo("name", name).get().await().toObjects(FacilityInd::class.java).firstOrNull()
        } catch (e: Exception) {
            println("Error fetching facility ind by name: ${e.message}")
            null
        }
    }

    // Create/Add FacilityInd
    suspend fun addFacilityInd(facilityInd: FacilityInd): Boolean {
        return try {
            facilityIndCollection.document(facilityInd.id).set(facilityInd).await()
            true
        } catch (e: Exception) {
            println("Error adding facility ind: ${e.message}")
            false
        }
    }

    // Update FacilityInd
    suspend fun updateFacilityInd(id: String, updates: Map<String, Any>): Boolean {
        return try {
            facilityIndCollection.document(id).update(updates).await()
            true
        } catch (e: Exception) {
            println("Error updating facility ind: ${e.message}")
            false
        }
    }

    // Delete FacilityInd
    suspend fun deleteFacilityInd(id: String): Boolean {
        return try {
            facilityIndCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            println("Error deleting facility ind: ${e.message}")
            false
        }
    }

    // Get FacilityInd by prefix (e.g., "S1" from "S1_1")
    suspend fun getFacilityIndByPrefix(prefix: String): List<FacilityInd> {
        return try {
            getAllFacilityInds().filter { it.id.substringBefore("_") == prefix }
        } catch (e: Exception) {
            println("Error fetching facility inds by prefix: ${e.message}")
            emptyList()
        }
    }
}