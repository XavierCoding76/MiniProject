package com.example.miniproject.admin.facilityAdmin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.facility.Facility
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FacilityListViewModel : ViewModel() {
    private val _facilities = mutableStateOf<List<Facility>>(emptyList())
    val facilities: State<List<Facility>> = _facilities

    // For Clubhouse (C)
    private val _clubhouseFacilities = mutableStateOf<List<Facility>>(emptyList())
    val clubhouseFacilities: State<List<Facility>> = _clubhouseFacilities

    // For Lecture Hall (L)
    private val _lectureHallFacilities = mutableStateOf<List<Facility>>(emptyList())
    val lectureHallFacilities: State<List<Facility>> = _lectureHallFacilities

    // For Sports Facility (S)
    private val _sportsFacilities = mutableStateOf<List<Facility>>(emptyList())
    val sportsFacilities: State<List<Facility>> = _sportsFacilities

    // For Computer Lab (CC)
    private val _computerLabFacilities = mutableStateOf<List<Facility>>(emptyList())
    val computerLabFacilities: State<List<Facility>> = _computerLabFacilities

    fun fetchFacilitiesStartingWith(prefix: String) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                // Querying a range from the prefix to a high-unicode character works as a "starts with" query.
                val querySnapshot = db.collection("facility")
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), prefix)
                    .whereLessThan(FieldPath.documentId(), prefix + "\uf8ff")
                    .orderBy(FieldPath.documentId())
                    .get()
                    .await()

                val fetchedFacilities = querySnapshot.toObjects(Facility::class.java)

                // Filter based on the specific prefix to avoid overlap (e.g., 'C' also matching 'CC').
                val filteredFacilities = when (prefix) {
                    "C" -> fetchedFacilities.filter { !it.id.startsWith("CC") }
                    "L" -> fetchedFacilities
                    "S" -> fetchedFacilities
                    "CC" -> fetchedFacilities
                    else -> fetchedFacilities
                }

                // Update the appropriate state based on prefix
                when (prefix) {
                    "C" -> _clubhouseFacilities.value = filteredFacilities
                    "L" -> _lectureHallFacilities.value = filteredFacilities
                    "S" -> _sportsFacilities.value = filteredFacilities
                    "CC" -> _computerLabFacilities.value = filteredFacilities
                }

                // Also update the general facilities list for backward compatibility
                _facilities.value = filteredFacilities
            } catch (e: Exception) {
                println("Error fetching facilities with prefix $prefix: ${e.message}")
                when (prefix) {
                    "C" -> _clubhouseFacilities.value = emptyList()
                    "L" -> _lectureHallFacilities.value = emptyList()
                    "S" -> _sportsFacilities.value = emptyList()
                    "CC" -> _computerLabFacilities.value = emptyList()
                }
                _facilities.value = emptyList()
            }
        }
    }

    /**
     * Adds a new facility to Firebase
     * @param facilityData Map containing all facility fields
     * @param onSuccess Callback when facility is successfully added
     */
    fun addNewFacility(
        facilityData: Map<String, Any>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore

                // Create Facility object from the map
                val facility = Facility(
                    id = facilityData["id"] as String,
                    name = facilityData["name"] as String,
                    description = facilityData["description"] as String,
                    location = facilityData["location"] as String,
                    minNum = facilityData["minNum"] as Int,
                    maxNum = facilityData["maxNum"] as Int,
                    startTime = facilityData["startTime"] as String,
                    endTime = facilityData["endTime"] as String
                )

                // Save to Firebase
                db.collection("facility")
                    .document(facility.id)
                    .set(facility)
                    .await()

                println("Facility ${facility.id} added successfully!")

                // Call success callback on main thread
                onSuccess()
            } catch (e: Exception) {
                println("Error adding facility: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}