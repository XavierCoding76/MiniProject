package com.example.miniproject.admin.facilityAdmin

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.facility.Facility
import com.example.miniproject.facility.FacilityInd
import com.example.miniproject.facility.FacilityIndRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SubVenuesViewModel : ViewModel() {

    var subVenues by mutableStateOf<List<FacilityInd>>(emptyList())
    var mainFacility by mutableStateOf<Facility?>(null)

    private val facilityIndRepository = FacilityIndRepository()

    fun fetchSubVenues(mainFacilityId: String) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore

                // Fetch main facility
                val mainFacilityDoc = db.collection("facility").document(mainFacilityId).get().await()
                mainFacility = mainFacilityDoc.toObject(Facility::class.java)

                // Fetch all facilityind documents
                val allFacilityIndSnapshot = db.collection("facilityind").get().await()
                val allFacilityInds = allFacilityIndSnapshot.toObjects(FacilityInd::class.java)

                println("=== DEBUG INFO ===")
                println("Main Facility ID: $mainFacilityId")
                println("Total FacilityInd documents: ${allFacilityInds.size}")

                // Filter FacilityInd objects that match the main facility
                subVenues = allFacilityInds.filter { facilityInd ->
                    val facilityIndId = facilityInd.id ?: ""

                    // Strip to get prefix (e.g., "S1_1" → "S1")
                    val prefix = facilityIndId.substringBefore("_")

                    // Check if it matches and has underscore
                    val matches = prefix == mainFacilityId && facilityIndId.contains("_")

                    if (matches) {
                        println("✓ Match found: $facilityIndId - ${facilityInd.name} | customMin: ${facilityInd.customMinNum}, customMax: ${facilityInd.customMaxNum}")
                    }

                    matches
                }.sortedBy { it.name }

                println("Found ${subVenues.size} sub-venues: ${subVenues.map { it.name }}")
                println("=================")
            } catch (e: Exception) {
                println("Error fetching sub-venues: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun addSubVenue(
        mainFacilityId: String,
        name: String,
        customMinNum: Int,
        customMaxNum: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val repository = FacilityIndRepository()

                // Get existing sub-venues to determine the next number
                val existingSubVenues = repository.getFacilityIndByPrefix(mainFacilityId)

                // Extract the numbers from existing sub-venue IDs (e.g., "S1_1" -> 1, "S1_2" -> 2)
                val existingNumbers = existingSubVenues.mapNotNull { subVenue ->
                    subVenue.id.substringAfter("_", "").toIntOrNull()
                }.toSet()

                // Find the next available number (fills gaps)
                var nextNum = 1
                while (existingNumbers.contains(nextNum)) {
                    nextNum++
                }

                // Create the new sub-venue ID (e.g., "S1_1")
                val newSubVenueId = "${mainFacilityId}_$nextNum"

                // Create the FacilityInd object
                val newSubVenue = FacilityInd(
                    id = newSubVenueId,
                    name = name,
                    customMinNum = customMinNum,
                    customMaxNum = customMaxNum
                )

                // Add to Firestore
                val success = repository.addFacilityInd(newSubVenue)

                if (success) {
                    Log.d("SubVenuesViewModel", "Sub-venue added successfully: $newSubVenueId")
                    onSuccess()
                } else {
                    onError("Failed to add sub-venue")
                }

            } catch (e: Exception) {
                Log.e("SubVenuesViewModel", "Error adding sub-venue", e)
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun updateCustomCapacity(
        facilityIndId: String,
        customMinNum: Int,
        customMaxNum: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "customMinNum" to customMinNum,
                    "customMaxNum" to customMaxNum
                )

                val success = facilityIndRepository.updateFacilityInd(facilityIndId, updates)

                if (success) {
                    println("✓ Successfully updated capacity for $facilityIndId: min=$customMinNum, max=$customMaxNum")

                    // Refresh the subVenues list to reflect the changes
                    mainFacility?.id?.let { fetchSubVenues(it) }

                    onSuccess()
                } else {
                    println("✗ Failed to update capacity for $facilityIndId")
                    onError("Failed to update capacity")
                }
            } catch (e: Exception) {
                println("Error updating custom capacity: ${e.message}")
                e.printStackTrace()
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun clearCustomCapacity(
        facilityIndId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // Set both to 0 when clearing
        updateCustomCapacity(facilityIndId, 0, 0, onSuccess, onError)
    }

    fun deleteSubVenue(
        facilityIndId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val success = facilityIndRepository.deleteFacilityInd(facilityIndId)

                if (success) {
                    Log.d("SubVenuesViewModel", "Sub-venue deleted successfully: $facilityIndId")
                    onSuccess()
                } else {
                    Log.e("SubVenuesViewModel", "Failed to delete sub-venue: $facilityIndId")
                    onError("Failed to delete sub-venue")
                }
            } catch (e: Exception) {
                Log.e("SubVenuesViewModel", "Error deleting sub-venue", e)
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
