package com.example.miniproject.admin.facilityAdmin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FacilityDetailViewModel : ViewModel() {

    var facilityId by mutableStateOf<String?>(null)
    var facility by mutableStateOf<Map<String, Any>?>(null)
    var facilityExists by mutableStateOf(true)
    var hasEquipment by mutableStateOf<Boolean?>(null)
    var equipmentList by mutableStateOf<List<Map<String, Any>>>(emptyList())
    var hasSubVenues by mutableStateOf(false)

    fun fetchFacilityByName(name: String) {
        println("Searching for facility with name: '$name'")
        viewModelScope.launch {
            val db = Firebase.firestore
            try {
                val querySnapshot = db.collection("facility")
                    .whereEqualTo("name", name)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    facilityExists = false
                } else {
                    val doc = querySnapshot.documents[0]
                    facilityId = doc.id
                    facility = doc.data
                    facilityExists = true
                    fetchEquipment(doc.id)
                    checkForSubVenues(doc.id)
                }
            } catch (e: Exception) {
                facilityExists = false
                println("Error fetching facility: ${e.message}")
            }
        }
    }

    private fun fetchEquipment(facilityId: String) {
        viewModelScope.launch {
            try {
                val equipmentQuery = Firebase.firestore.collection("equipment")
                    .whereEqualTo("facilityID", facilityId)
                    .get()
                    .await()
                equipmentList = equipmentQuery.documents.map { doc -> doc.data!!.plus("id" to doc.id) }
                hasEquipment = equipmentList.isNotEmpty()
            } catch (e: Exception) {
                println("Error fetching equipment: ${e.message}")
                hasEquipment = false
            }
        }
    }

    private fun checkForSubVenues(facilityId: String) {
        viewModelScope.launch {
            try {
                val subVenuesQuery = Firebase.firestore.collection("facility")
                    .whereGreaterThan(FieldPath.documentId(), facilityId + "_")
                    .whereLessThan(FieldPath.documentId(), facilityId + "_\uf8ff")
                    .limit(1)
                    .get()
                    .await()
                hasSubVenues = !subVenuesQuery.isEmpty
            } catch (e: Exception) {
                println("Error checking for sub-venues: ${e.message}")
                hasSubVenues = false
            }
        }
    }

    fun saveEquipmentChanges(newEquipmentData: List<Map<String, Any>>, onComplete: () -> Unit) {
        val facilityId = this.facilityId ?: return
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val batch = db.batch()
                val originalIds = equipmentList.mapNotNull { it["id"] as? String }.toSet()

                val newItems = newEquipmentData.filter { !it.containsKey("id") }
                val existingItems = newEquipmentData.filter { it.containsKey("id") }

                // Add new items
                newItems.forEach {
                    val docRef = db.collection("equipment").document()
                    batch.set(docRef, it.plus("facilityID" to facilityId))
                }

                // Update existing items
                existingItems.forEach {
                    val id = it["id"] as String
                    batch.update(db.collection("equipment").document(id), "name", it["name"] as Any)
                }

                // Delete removed items
                val newIds = existingItems.mapNotNull { it["id"] as? String }.toSet()
                val toDelete = originalIds - newIds
                toDelete.forEach {
                    batch.delete(db.collection("equipment").document(it))
                }

                batch.commit().await()
            } catch (e: Exception) {
                println("Error saving equipment changes: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    fun updateVenueDetails(newData: Map<String, Any>, onComplete: () -> Unit) {
        if (facilityId == null) {
            onComplete()
            return
        }
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("facility").document(facilityId!!)
                    .update(newData)
                    .await()
            } catch (e: Exception) {
                println("Error updating facility details: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }
}
