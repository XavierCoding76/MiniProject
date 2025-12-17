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
                val filteredFacilities = if (prefix == "C") {
                    fetchedFacilities.filter { !it.id.startsWith("CC") }
                } else {
                    fetchedFacilities
                }

                _facilities.value = filteredFacilities
            } catch (e: Exception) {
                println("Error fetching facilities with prefix $prefix: ${e.message}")
                _facilities.value = emptyList()
            }
        }
    }
}
