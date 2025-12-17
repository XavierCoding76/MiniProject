package com.example.miniproject.admin.facilityAdmin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.facility.Facility
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SubVenuesViewModel : ViewModel() {

    var subVenues by mutableStateOf<List<Facility>>(emptyList())
    var mainFacility by mutableStateOf<Facility?>(null)

    fun fetchSubVenues(mainFacilityId: String) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val mainFacilityDoc = db.collection("facility").document(mainFacilityId).get().await()
                mainFacility = mainFacilityDoc.toObject(Facility::class.java)

                val allFacilitiesSnapshot = db.collection("facility").get().await()
                val allFacilities = allFacilitiesSnapshot.toObjects(Facility::class.java)

                subVenues = allFacilities
                    .filter { it.id.startsWith("${mainFacilityId}_") }
                    .sortedBy { it.name }
            } catch (e: Exception) {
                println("Error fetching sub-venues: ${e.message}")
            }
        }
    }
}
